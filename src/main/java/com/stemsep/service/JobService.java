package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.model.Job;
import com.stemsep.model.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private JobDao jobDao;

    @Autowired
    private ColabInferenceService colabService;

    @Value("${upload.directory:uploads}")
    private String uploadDirectory;

    @Transactional
    public Job createJob(String sessionId, MultipartFile file, String model) throws IOException {
        Path uploadDir = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        logger.info("[UPLOAD-1] cwd={}, uploadDir={}, exists={}",
            Paths.get("").toAbsolutePath(), uploadDir, Files.exists(uploadDir));
        Files.createDirectories(uploadDir);
        logger.info("[UPLOAD-2] uploadDir created/verified: writable={}", Files.isWritable(uploadDir));

        String safeName = file.getOriginalFilename() == null ? "audio" : file.getOriginalFilename();
        String filename = System.currentTimeMillis() + "_" + safeName;
        Path filePath = uploadDir.resolve(filename).toAbsolutePath().normalize();
        logger.info("[UPLOAD-3] target absolute path={}, originalSize={} bytes",
            filePath, file.getSize());

        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            logger.error("[UPLOAD-FAIL] transferTo failed for {}: {}", filePath, e.toString());
            // Spring/Tomcat fallback: stream copy directly
            try (var in = file.getInputStream()) {
                Files.copy(in, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("[UPLOAD-3b] stream-copy fallback succeeded");
            }
        }
        logger.info("[UPLOAD-4] saved {} bytes to {}",
            Files.size(filePath), filePath);

        Job job = new Job();
        job.setSessionId(sessionId);
        job.setOriginalFilename(file.getOriginalFilename());
        job.setOriginalFilePath(filePath.toString());
        job.setModelUsed(model);
        job.setStatus(JobStatus.PENDING);

        jobDao.save(job);
        logger.info("[UPLOAD-5] Job created: id={}, file={}, model={}", job.getId(), file.getOriginalFilename(), model);

        return job;
    }

    @Async
    public void processJobAsync(Long jobId) {
        try {
            colabService.processJob(jobId);
        } catch (Exception e) {
            logger.error("Async processing error for job {}: {}", jobId, e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Job getJob(Long id) {
        return jobDao.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Job> getJobsBySession(String sessionId) {
        return jobDao.findBySessionId(sessionId);
    }

    @Transactional
    public void updateJobStatus(Long jobId, JobStatus status) {
        Job job = jobDao.findById(jobId);
        if (job != null) {
            job.setStatus(status);
            if (status == JobStatus.COMPLETED) {
                job.setCompletedAt(LocalDateTime.now());
            }
            jobDao.update(job);
        }
    }
}
