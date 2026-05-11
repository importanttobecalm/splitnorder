package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.dao.StemDao;
import com.stemsep.exception.InferenceFailedException;
import com.stemsep.model.Job;
import com.stemsep.model.JobStatus;
import com.stemsep.model.Stem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
            throw new InferenceFailedException("Job bulunamadı: " + jobId);
        }
        job.setStatus(JobStatus.PROCESSING);
        jobDao.update(job);

        try {
            callSeparate(job);
            createStemRecords(job);
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            jobDao.update(job);
            logger.info("Job {} tamamlandı", jobId);
        } catch (IOException e) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            jobDao.update(job);
            throw new InferenceFailedException("Demucs çağrısı başarısız: " + e.getMessage());
        }
    }

    private void callSeparate(Job job) throws IOException {
        URL url = new URL(colabApiUrl + "/api/separate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("ngrok-skip-browser-warning", "true");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(120000);

        String payload = String.format(
                "{\"file_path\":\"%s\",\"model\":\"%s\",\"job_id\":%d}",
                job.getOriginalFilePath().replace("\\", "/"),
                job.getModelUsed(),
                job.getId());
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new IOException("Demucs API HTTP " + code);
        }
        logger.info("Demucs API job {} tamamlandı (HTTP 200)", job.getId());
    }

    private void createStemRecords(Job job) throws IOException {
        Path stemsDir = Paths.get(stemsDirectory, String.valueOf(job.getId())).toAbsolutePath();
        Files.createDirectories(stemsDir);
        for (String stemType : new String[]{"vocals", "drums", "bass", "other"}) {
            Path file = stemsDir.resolve(stemType + ".wav");
            Stem stem = new Stem();
            stem.setJob(job);
            stem.setStemType(stemType);
            stem.setFilePath(file.toString());
            stem.setFileSize(file.toFile().exists() ? file.toFile().length() : 0L);
            stem.setDownloadUrl("/job/" + job.getId() + "/download/" + stemType);
            stemDao.save(stem);
            job.getStems().add(stem);
        }
    }
}
