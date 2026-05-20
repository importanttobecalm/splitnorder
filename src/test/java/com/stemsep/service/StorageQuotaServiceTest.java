package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.dao.MixedTrackDao;
import com.stemsep.dao.StemDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * {@link StorageQuotaService} birim testleri.
 *
 * <p><b>Slayt referansı:</b> {@code criteriaBuilder.sum(root.get("alan"))}
 * SUM projeksiyonu (8.pdf — sum Projeksiyonu). Service iki DAO'dan gelen
 * SUM sonuçlarını toplar; bu test DAO'ları mock'layıp kombinasyonları
 * doğrular.</p>
 *
 * <p>Kapsanan senaryolar: 0 kullanım, kısmi kullanım, dolu (>=quota),
 * over (used > quota — UI'da 100% gösterilmeli), wouldExceed sınır
 * kontrolü.</p>
 */
public class StorageQuotaServiceTest {

    private static final Long USER_ID = 42L;
    private static final long ONE_MB = 1024L * 1024L;
    private static final long ONE_GB = 1024L * ONE_MB;

    @Mock
    private JobDao jobDao;

    @Mock
    private StemDao stemDao;

    @Mock
    private MixedTrackDao mixedTrackDao;

    @InjectMocks
    private StorageQuotaService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mixedTrackDao.sumFileSizeByUserId(USER_ID)).thenReturn(0L);
    }

    /** Hiç dosya yoksa kullanım 0 olmalı (SUM NULL → DAO 0L döner). */
    @Test
    public void getUsedBytes_zeroWhenNoFiles() {
        when(jobDao.sumOriginalFileSizeByUserId(USER_ID)).thenReturn(0L);
        when(stemDao.sumFileSizeByUserId(USER_ID)).thenReturn(0L);
        assertEquals(0L, service.getUsedBytes(USER_ID));
    }

    /** Orijinaller + stem'ler ayrı ayrı toplamalı (10 MB + 30 MB = 40 MB). */
    @Test
    public void getUsedBytes_sumsOriginalAndStems() {
        when(jobDao.sumOriginalFileSizeByUserId(USER_ID)).thenReturn(10L * ONE_MB);
        when(stemDao.sumFileSizeByUserId(USER_ID)).thenReturn(30L * ONE_MB);
        assertEquals(40L * ONE_MB, service.getUsedBytes(USER_ID));
    }

    /** Kalan = quota - used; negatife düşmemeli (clamp at 0). */
    @Test
    public void getRemainingBytes_clampsAtZeroWhenOverQuota() {
        when(jobDao.sumOriginalFileSizeByUserId(USER_ID)).thenReturn(6L * ONE_GB);
        when(stemDao.sumFileSizeByUserId(USER_ID)).thenReturn(0L);
        assertEquals(0L, service.getRemainingBytes(USER_ID));
    }

    /** 2.5 GB / 5 GB = %50 olmalı (integer division ile). */
    @Test
    public void getUsagePercent_halfQuota_returns50() {
        when(jobDao.sumOriginalFileSizeByUserId(USER_ID)).thenReturn(2L * ONE_GB + 512L * ONE_MB);
        when(stemDao.sumFileSizeByUserId(USER_ID)).thenReturn(0L);
        assertEquals(50, service.getUsagePercent(USER_ID));
    }

    /** Over-quota durumunda yüzde 100 ile sınırlanmalı (UI bar > 100% olmaz). */
    @Test
    public void getUsagePercent_clampsAt100WhenOverQuota() {
        when(jobDao.sumOriginalFileSizeByUserId(USER_ID)).thenReturn(7L * ONE_GB);
        when(stemDao.sumFileSizeByUserId(USER_ID)).thenReturn(0L);
        assertEquals(100, service.getUsagePercent(USER_ID));
    }

    /** 4 GB kullanım + 500 MB yeni upload → 4.5 GB toplam, kotaya sığar (5 GB). */
    @Test
    public void wouldExceed_fitsBelowQuota_returnsFalse() {
        when(jobDao.sumOriginalFileSizeByUserId(USER_ID)).thenReturn(4L * ONE_GB);
        when(stemDao.sumFileSizeByUserId(USER_ID)).thenReturn(0L);
        assertFalse(service.wouldExceed(USER_ID, 500L * ONE_MB));
    }

    /** 4.9 GB kullanım + 200 MB yeni upload → 5.1 GB, kotayı aşar. */
    @Test
    public void wouldExceed_exceedsQuota_returnsTrue() {
        when(jobDao.sumOriginalFileSizeByUserId(USER_ID)).thenReturn(4L * ONE_GB + 900L * ONE_MB);
        when(stemDao.sumFileSizeByUserId(USER_ID)).thenReturn(0L);
        assertTrue(service.wouldExceed(USER_ID, 200L * ONE_MB));
    }
}
