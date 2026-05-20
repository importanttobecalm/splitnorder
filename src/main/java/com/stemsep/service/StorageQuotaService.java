package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.dao.StemDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageQuotaService {

    public static final long QUOTA_BYTES = 5L * 1024L * 1024L * 1024L; // 5 GB
    public static final int WARN_PERCENT = 80;

    @Autowired
    private JobDao jobDao;

    @Autowired
    private StemDao stemDao;

    @Transactional(readOnly = true)
    public long getUsedBytes(Long userId) {
        long original = jobDao.sumOriginalFileSizeByUserId(userId);
        long stems = stemDao.sumFileSizeByUserId(userId);
        return original + stems;
    }

    @Transactional(readOnly = true)
    public long getRemainingBytes(Long userId) {
        return Math.max(0L, QUOTA_BYTES - getUsedBytes(userId));
    }

    @Transactional(readOnly = true)
    public int getUsagePercent(Long userId) {
        long used = getUsedBytes(userId);
        return (int) Math.min(100L, (used * 100L) / QUOTA_BYTES);
    }

    @Transactional(readOnly = true)
    public boolean wouldExceed(Long userId, long additionalBytes) {
        return getUsedBytes(userId) + additionalBytes > QUOTA_BYTES;
    }
}
