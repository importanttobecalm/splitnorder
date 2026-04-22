package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.dao.StemDao;
import com.stemsep.model.Job;
import com.stemsep.model.JobStatus;
import com.stemsep.model.Stem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Lokal Demucs Flask API sunucusu ile iletişim kuran servis.
 *
 * Flask API akışı:
 * 1. POST /api/separate → 202 Accepted (işlem arka planda başlar)
 * 2. GET /api/job/{id}/status → Durum yoklaması (polling)
 * 3. İşlem tamamlanınca stem kayıtlarını oluştur
 */
@Service
public class ColabInferenceService {

    private static final Logger logger = LoggerFactory.getLogger(ColabInferenceService.class);

    private static final int POLL_INTERVAL_MS = 3000;      // 3 saniye aralıkla yokla
    private static final int MAX_POLL_ATTEMPTS = 600;       // Max 30 dakika (600 * 3s)

    @Autowired
    private JobDao jobDao;

    @Autowired
    private StemDao stemDao;

    @Value("${colab.api.url:http://localhost:5000}")
    private String colabApiUrl;

    @Value("${stems.directory:stems}")
    private String stemsDirectory;

    @Transactional
    public void processJob(Long jobId) {
        Job job = jobDao.findById(jobId);
        if (job == null) {
            logger.error("Job not found: {}", jobId);
            return;
        }

        try {
            job.setStatus(JobStatus.PROCESSING);
            jobDao.update(job);

            logger.info("Processing job {}: file={}, model={}", jobId, job.getOriginalFilename(), job.getModelUsed());

            // 1. Flask API'ye stem ayırma isteği gönder
            boolean accepted = sendSeparateRequest(job);

            if (!accepted) {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("Demucs API isteği reddedildi");
                jobDao.update(job);
                return;
            }

            // 2. İşlem tamamlanana kadar yokla (polling)
            boolean completed = pollUntilComplete(job);

            if (completed) {
                // 3. Stem kayıtlarını oluştur
                createStemRecords(job);
                job.setStatus(JobStatus.COMPLETED);
                job.setCompletedAt(LocalDateTime.now());
                logger.info("Job {} completed successfully", jobId);
            } else {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("İşlem zaman aşımına uğradı veya başarısız oldu");
                logger.error("Job {} failed or timed out", jobId);
            }

            jobDao.update(job);

        } catch (Exception e) {
            logger.error("Job {} failed: {}", jobId, e.getMessage(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            jobDao.update(job);
        }
    }

    /**
     * Flask API'ye POST /api/separate isteği gönder.
     * Flask 202 Accepted döndürürse true, aksi halde false.
     */
    private boolean sendSeparateRequest(Job job) {
        try {
            URL url = new URL(colabApiUrl + "/api/separate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            String jsonPayload = String.format(
                "{\"file_path\":\"%s\",\"model\":\"%s\",\"job_id\":%d}",
                job.getOriginalFilePath().replace("\\", "/"),
                job.getModelUsed(),
                job.getId()
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            String responseBody = readResponse(conn);

            if (responseCode == 200 || responseCode == 202) {
                logger.info("Demucs API accepted job {}: {}", job.getId(), responseBody);
                return true;
            } else {
                logger.error("Demucs API rejected job {} (HTTP {}): {}", job.getId(), responseCode, responseBody);
                return false;
            }

        } catch (IOException e) {
            logger.warn("Demucs API not available, falling back to mock mode for job {}: {}", job.getId(), e.getMessage());
            // Mock mode: Demucs API çalışmıyorsa simüle et
            return mockProcessing(job);
        }
    }

    /**
     * Flask API'den iş durumunu yokla (polling).
     * İşlem tamamlanana veya hata oluşana kadar devam eder.
     */
    private boolean pollUntilComplete(Job job) {
        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);

                URL url = new URL(colabApiUrl + "/api/job/" + job.getId() + "/status");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    logger.warn("Poll attempt {} for job {} returned HTTP {}", attempt, job.getId(), responseCode);
                    continue;
                }

                String responseBody = readResponse(conn);
                logger.debug("Poll job {} attempt {}: {}", job.getId(), attempt, responseBody);

                // Basit JSON parse (Jackson kullanmadan)
                if (responseBody.contains("\"status\": \"completed\"") ||
                    responseBody.contains("\"status\":\"completed\"")) {
                    logger.info("Job {} completed after {} poll attempts", job.getId(), attempt + 1);
                    return true;
                }

                if (responseBody.contains("\"status\": \"failed\"") ||
                    responseBody.contains("\"status\":\"failed\"")) {
                    logger.error("Job {} failed on Demucs API: {}", job.getId(), responseBody);
                    return false;
                }

                // Hala işleniyor, devam et

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (IOException e) {
                logger.warn("Poll error for job {} (attempt {}): {}", job.getId(), attempt, e.getMessage());
                // Bir kaç hata tolerans göster, devam et
            }
        }

        logger.error("Job {} timed out after {} attempts", job.getId(), MAX_POLL_ATTEMPTS);
        return false;
    }

    /**
     * HTTP yanıtını oku.
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        InputStream is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) return "";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    /**
     * Mock mode: Demucs API çalışmıyorsa geliştirme amaçlı simülasyon.
     */
    private boolean mockProcessing(Job job) {
        try {
            logger.info("Mock processing job {} (Demucs API not connected)", job.getId());
            Thread.sleep(3000); // Simüle et
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Stem dosyası kayıtlarını veritabanında oluştur.
     * Flask API stem dosyalarını stems/{job_id}/ dizinine kaydeder.
     */
    private void createStemRecords(Job job) throws IOException {
        Path stemsDir = Paths.get(stemsDirectory, String.valueOf(job.getId()));
        Files.createDirectories(stemsDir);

        String[] stemTypes = {"vocals", "drums", "bass", "other"};

        for (String stemType : stemTypes) {
            Path stemFile = stemsDir.resolve(stemType + ".wav");

            Stem stem = new Stem();
            stem.setJob(job);
            stem.setStemType(stemType);
            stem.setFilePath(stemFile.toString());
            stem.setFileSize(stemFile.toFile().exists() ? stemFile.toFile().length() : 0L);
            stem.setDownloadUrl("/job/" + job.getId() + "/download/" + stemType);

            stemDao.save(stem);
            job.getStems().add(stem);

            if (stemFile.toFile().exists()) {
                logger.info("Stem record created: job={}, type={}, size={} bytes",
                    job.getId(), stemType, stemFile.toFile().length());
            } else {
                logger.warn("Stem file not found on disk: {}", stemFile);
            }
        }
    }
}
