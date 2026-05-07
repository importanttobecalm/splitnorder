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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
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
        logger.info("[GPU-1] job={} → audioFile={}, exists={}, size={}",
            job.getId(), audioFile.getAbsolutePath(), audioFile.exists(),
            audioFile.exists() ? audioFile.length() : -1);
        if (!audioFile.exists()) {
            logger.error("[GPU-FAIL] Audio file missing for job {}: {}", job.getId(), audioFile.getAbsolutePath());
            return false;
        }

        long start = System.currentTimeMillis();
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(300000);
            RestTemplate rest = new RestTemplate(factory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("ngrok-skip-browser-warning", "true");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(audioFile));
            body.add("model", job.getModelUsed());
            body.add("job_id", String.valueOf(job.getId()));

            String url = colabApiUrl + "/api/separate";
            logger.info("[GPU-2] POST multipart → {} (model={}, file={})", url, job.getModelUsed(), audioFile.getName());

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = rest.postForEntity(url, request, String.class);

            int code = response.getStatusCode().value();
            long elapsed = System.currentTimeMillis() - start;
            logger.info("[GPU-3] response in {}ms — HTTP {} → {}", elapsed, code, response.getBody());
            if (code == 200 || code == 202) {
                logger.info("[GPU-4] job {} accepted by GPU API", job.getId());
                return true;
            }
            logger.error("[GPU-FAIL] job {} rejected (HTTP {}): {}", job.getId(), code, response.getBody());
            return false;

        } catch (HttpStatusCodeException e) {
            // GPU API'nin verdiği gerçek 4xx/5xx — kullanıcıya net hata göster, mock'a düşme
            long elapsed = System.currentTimeMillis() - start;
            String body = e.getResponseBodyAsString();
            logger.error("[GPU-FAIL] GPU rejected job {} after {}ms — HTTP {} {}", job.getId(), elapsed, e.getStatusCode().value(), body);
            job.setErrorMessage("GPU API: " + body);
            return false;
        } catch (ResourceAccessException e) {
            // Network / timeout — Kaggle ulaşılamıyor olabilir, mock'a düş
            long elapsed = System.currentTimeMillis() - start;
            logger.warn("[GPU-FAIL] GPU unreachable after {}ms ({}) — mock mode'a düşülüyor", elapsed, e.getMessage());
            return mockProcessing(job);
        } catch (RestClientException e) {
            long elapsed = System.currentTimeMillis() - start;
            logger.error("[GPU-FAIL] Unexpected GPU client error after {}ms: {}", elapsed, e.toString());
            job.setErrorMessage("GPU API client error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Flask API'den iş durumunu yokla (polling).
     * İşlem tamamlanana veya hata oluşana kadar devam eder.
     */
    private boolean pollUntilComplete(Job job) {
        long start = System.currentTimeMillis();
        String lastMessage = "";
        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);

                URL url = new URL(colabApiUrl + "/api/job/" + job.getId() + "/status");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("ngrok-skip-browser-warning", "true");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    logger.warn("[POLL] job={} attempt={} HTTP {}", job.getId(), attempt, responseCode);
                    continue;
                }

                String responseBody = readResponse(conn);

                // Progress + message değiştiğinde tek satır basıt log (sürekli aynı mesajı tekrarlamaz)
                String prog = extractField(responseBody, "progress");
                String msg = extractField(responseBody, "message");
                String key = prog + "|" + msg;
                if (!key.equals(lastMessage)) {
                    long elapsed = (System.currentTimeMillis() - start) / 1000;
                    logger.info("[POLL] job={} t+{}s progress={}% — {}", job.getId(), elapsed, prog, msg);
                    lastMessage = key;
                }

                if (responseBody.contains("\"status\": \"completed\"") ||
                    responseBody.contains("\"status\":\"completed\"")) {
                    logger.info("[POLL-OK] job={} completed in {}s after {} polls", job.getId(),
                        (System.currentTimeMillis() - start) / 1000, attempt + 1);
                    return true;
                }

                if (responseBody.contains("\"status\": \"failed\"") ||
                    responseBody.contains("\"status\":\"failed\"")) {
                    logger.error("[POLL-FAIL] job={} GPU reports failed: {}", job.getId(), responseBody);
                    return false;
                }

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
     * Basit JSON field çıkarıcı (Jackson olmadan). Hem string hem sayı değerleri yakalar.
     * Örn. "progress":50 → "50", "message":"x" → "x".
     */
    private String extractField(String json, String key) {
        if (json == null) return "";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*(?:\"([^\"]*)\"|([\\d.]+))"
        ).matcher(json);
        if (m.find()) {
            return m.group(1) != null ? m.group(1) : m.group(2);
        }
        return "";
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
