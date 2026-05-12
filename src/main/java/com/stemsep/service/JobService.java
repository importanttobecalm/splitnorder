package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.model.Job;
import com.stemsep.model.User;
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
    public Job createJob(User user, MultipartFile file, String model) throws IOException {
        Path uploadDir = Paths.get(uploadDirectory).toAbsolutePath();
        Files.createDirectories(uploadDir);

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());

        Job job = new Job();
        job.setUser(user);
        job.setOriginalFilename(file.getOriginalFilename());
        job.setOriginalFilePath(filePath.toString());
        job.setModelUsed(model);
        job.setStatus(JobStatus.PENDING);

        jobDao.save(job);
        logger.info("Job created: id={}, file={}, model={}", job.getId(), file.getOriginalFilename(), model);

        return job;
    }

    public void processJobAsync(Long jobId) {
        // Manuel fire-and-forget thread: @EnableAsync olmadan Spring @Async hiç çalışmıyor.
        // Demucs 8 sn → request thread'i 8 sn bekletmemek için ayrı thread.
        Thread t = new Thread(() -> {
            try {
                colabService.processJob(jobId);
            } catch (Exception e) {
                logger.error("Async processing error for job {}: {}", jobId, e.getMessage(), e);
            }
        }, "demucs-job-" + jobId);
        t.setDaemon(true);
        t.start();
    }

    @Transactional(readOnly = true)
    public Job getJob(Long id) {
        return jobDao.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Job> getJobsByUser(Long userId) {
        return jobDao.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Job> searchJobsByUser(Long userId, String query) {
        return jobDao.findByUserIdAndQuery(userId, query);
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
