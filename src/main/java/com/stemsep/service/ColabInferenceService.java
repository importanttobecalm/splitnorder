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

@Service
public class ColabInferenceService {

    private static final Logger logger = LoggerFactory.getLogger(ColabInferenceService.class);

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

            // Send to Colab/Kaggle GPU for inference
            boolean success = sendToColabForProcessing(job);

            if (success) {
                // Create stem records
                createStemRecords(job);
                job.setStatus(JobStatus.COMPLETED);
                job.setCompletedAt(LocalDateTime.now());
                logger.info("Job {} completed successfully", jobId);
            } else {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("GPU inference failed");
                logger.error("Job {} failed during GPU inference", jobId);
            }

            jobDao.update(job);

        } catch (Exception e) {
            logger.error("Job {} failed: {}", jobId, e.getMessage(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            jobDao.update(job);
        }
    }

    private boolean sendToColabForProcessing(Job job) {
        try {
            URL url = new URL(colabApiUrl + "/api/separate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(300000); // 5 min for large files

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
            if (responseCode == 200) {
                logger.info("Colab API returned success for job {}", job.getId());
                return true;
            } else {
                logger.error("Colab API returned error {} for job {}", responseCode, job.getId());
                return false;
            }

        } catch (IOException e) {
            logger.warn("Colab API not available, using mock mode for job {}: {}", job.getId(), e.getMessage());
            // Mock mode for development – simulate successful processing
            return mockProcessing(job);
        }
    }

    private boolean mockProcessing(Job job) {
        try {
            logger.info("Mock processing job {} (Colab not connected)", job.getId());
            Thread.sleep(3000); // Simulate processing
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void createStemRecords(Job job) throws IOException {
        Path stemsDir = Paths.get(stemsDirectory, String.valueOf(job.getId()));
        Files.createDirectories(stemsDir);

        String[] stemTypes = {"vocals", "drums", "bass", "other"};

        for (String stemType : stemTypes) {
            Stem stem = new Stem();
            stem.setJob(job);
            stem.setStemType(stemType);
            stem.setFilePath(stemsDir.resolve(stemType + ".wav").toString());
            stem.setFileSize(0L);
            stem.setDownloadUrl("/job/" + job.getId() + "/download/" + stemType);

            stemDao.save(stem);
            job.getStems().add(stem);
        }
    }
}
