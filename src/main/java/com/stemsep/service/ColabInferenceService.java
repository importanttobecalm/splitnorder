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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
     * Flask API'ye POST /api/separate isteği gönder (multipart upload).
     * Ses dosyası bytes olarak yüklenir — Flask aynı disk üzerinde olmak zorunda değil.
     */
    private boolean sendSeparateRequest(Job job) {
        File audioFile = new File(job.getOriginalFilePath());
        if (!audioFile.exists()) {
            logger.error("Audio file missing for job {}: {}", job.getId(), audioFile.getAbsolutePath());
            return false;
        }

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(120000);
            RestTemplate rest = new RestTemplate(factory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(audioFile));
            body.add("model", job.getModelUsed());
            body.add("job_id", String.valueOf(job.getId()));

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = rest.postForEntity(
                colabApiUrl + "/api/separate", request, String.class);

            int code = response.getStatusCode().value();
            if (code == 200 || code == 202) {
                logger.info("Demucs API accepted job {}: {}", job.getId(), response.getBody());
                return true;
            }
            logger.error("Demucs API rejected job {} (HTTP {}): {}", job.getId(), code, response.getBody());
            return false;

        } catch (RestClientException e) {
            logger.warn("Demucs API not available, falling back to mock mode for job {}: {}", job.getId(), e.getMessage());
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
     * Stem kayıtlarını DB'de oluştur. Dosyalar Colab'da kalır;
     * downloadUrl mutlak Colab URL'i olur — tarayıcı doğrudan oradan indirir.
     */
    private void createStemRecords(Job job) {
        String[] stemTypes = {"vocals", "drums", "bass", "other"};
        String base = colabApiUrl + "/api/stem/" + job.getId() + "/";

        for (String stemType : stemTypes) {
            Stem stem = new Stem();
            stem.setJob(job);
            stem.setStemType(stemType);
            stem.setFilePath(base + stemType);
            stem.setFileSize(0L);
            stem.setDownloadUrl(base + stemType);

            stemDao.save(stem);
            job.getStems().add(stem);

            logger.info("Stem record created: job={}, type={}, url={}", job.getId(), stemType, stem.getDownloadUrl());
        }
    }
}
