package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 3 aydan eski job'ları otomatik silen scheduled servis.
 *
 * <p>Her gün 03:00'da çalışır; {@code createdAt < now - retentionMonths}
 * koşulunu sağlayan tüm job'ları FS + DB cascade ile siler. Manuel
 * silme akışıyla aynı yolu kullanır ({@link JobService#deleteJob}).</p>
 *
 * <p>Politika frontend'de history sayfasında not olarak gösterilir
 * (TR/EN i18n: {@code retention.notice}).</p>
 */
@Service
public class JobRetentionService {

    private static final Logger logger = LoggerFactory.getLogger(JobRetentionService.class);

    @Autowired
    private JobDao jobDao;

    @Autowired
    private JobService jobService;

    @Value("${retention.months:3}")
    private int retentionMonths;

    /** Her gün 03:00'da: 3 aydan eski tüm job'ları sil. */
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(retentionMonths);
        List<Job> expired = findExpired(cutoff);

        if (expired.isEmpty()) {
            logger.info("Retention: silinecek eski job yok (cutoff={})", cutoff);
            return;
        }

        logger.info("Retention: {} job silinecek (cutoff={})", expired.size(), cutoff);
        int deleted = 0;
        int failed = 0;
        for (Job job : expired) {
            try {
                jobService.deleteJob(job.getPublicId(), job.getUser().getId());
                deleted++;
            } catch (IOException | RuntimeException e) {
                failed++;
                logger.warn("Retention silme başarısız: publicId={} hata={}",
                        job.getPublicId(), e.getMessage());
            }
        }
        logger.info("Retention tamamlandı: silinen={}, başarısız={}", deleted, failed);
    }

    @Transactional(readOnly = true)
    public List<Job> findExpired(LocalDateTime cutoff) {
        return jobDao.findOlderThan(cutoff);
    }
}
