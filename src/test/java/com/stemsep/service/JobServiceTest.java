package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.model.Job;
import com.stemsep.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link JobService} için birim testleri.
 *
 * <p>Service katmanı DAO + Inference servisi arasında iş kuralı orkestrasyonu
 * yapar. Bu testler Service'in:
 * <ul>
 *   <li>Job'u ID ile getirebildiğini,</li>
 *   <li>Job durumunu güncelleyebildiğini (PROCESSING → COMPLETED/FAILED),</li>
 *   <li>COMPLETED'a geçişte {@code completedAt} zamanını set ettiğini,</li>
 *   <li>FAILED'a geçişte {@code completedAt}'i set etmediğini,</li>
 *   <li>Olmayan Job için sessizce çıktığını (NPE fırlatmadığını)</li>
 * </ul>
 * doğrular.</p>
 *
 * <p><b>Slayt referansı:</b> Service Sınıfları (@Service + @Transactional) —
 * Controller'dan gelen iş süreçlerini karşılar, DAO ile veritabanına yazar.</p>
 */
public class JobServiceTest {

    @Mock
    private JobDao jobDao;

    @Mock
    private ColabInferenceService colabService;

    @InjectMocks
    private JobService jobService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /** Var olan Job'un ID ile getirilebildiğini ve alanlarının korunduğunu doğrular. */
    @Test
    public void testGetJobReturnsJobWhenExists() {
        Job job = new Job();
        job.setId(1L);
        job.setOriginalFilename("test.mp3");
        job.setStatus(JobStatus.PENDING);

        when(jobDao.findById(1L)).thenReturn(job);

        Job result = jobService.getJob(1L);
        assertNotNull(result);
        assertEquals("test.mp3", result.getOriginalFilename());
        assertEquals(JobStatus.PENDING, result.getStatus());
    }

    /** Olmayan Job için {@code null} dönüşü — controller bunu 404 sayfasına çevirir. */
    @Test
    public void testGetJobReturnsNullWhenNotExists() {
        when(jobDao.findById(999L)).thenReturn(null);

        Job result = jobService.getJob(999L);
        assertNull(result);
    }

    /**
     * Job COMPLETED'a geçince servis {@code completedAt} zamanını set eder ve
     * DAO {@code update}'i çağrılır. Bu, "ne zaman bitti?" gösteren UI için
     * gereklidir (history sayfasında tarih kolonu).
     */
    @Test
    public void testUpdateJobStatusToCompleted() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus(JobStatus.PROCESSING);

        when(jobDao.findById(1L)).thenReturn(job);

        jobService.updateJobStatus(1L, JobStatus.COMPLETED);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertNotNull(job.getCompletedAt());
        verify(jobDao).update(job);
    }

    /**
     * Job FAILED'a geçince servis {@code completedAt}'i SET ETMEZ — başarısız
     * görev "tamamlanmış" sayılmaz. Bu davranış kullanıcıya farklı UI mesajı
     * göstermek için önemli.
     */
    @Test
    public void testUpdateJobStatusToFailed() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus(JobStatus.PROCESSING);

        when(jobDao.findById(1L)).thenReturn(job);

        jobService.updateJobStatus(1L, JobStatus.FAILED);

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertNull(job.getCompletedAt());
        verify(jobDao).update(job);
    }

    /**
     * Olmayan Job için Service exception fırlatmaz, sadece DAO update'i
     * çağırmaz. Bu, sessiz no-op davranışı; Controller'da {@code getJob()}
     * zaten {@code null} dönüşü yakalar.
     */
    @Test
    public void testUpdateJobStatusNullJob() {
        when(jobDao.findById(999L)).thenReturn(null);

        jobService.updateJobStatus(999L, JobStatus.COMPLETED);

        verify(jobDao, never()).update(any());
    }

    /**
     * URL'lerde sıralı Long ID yerine UUID kullanmak için {@code publicId}
     * alanı eklendi — Service {@code getJobByPublicId} DAO'nun yeni lookup
     * metoduna delege etmeli. Controller bu yol üzerinden Job çözer.
     */
    @Test
    public void testGetJobByPublicIdReturnsJob() {
        Job job = new Job();
        job.setId(1L);
        job.setPublicId("11111111-2222-3333-4444-555555555555");
        job.setOriginalFilename("uuid.mp3");
        when(jobDao.findByPublicId("11111111-2222-3333-4444-555555555555")).thenReturn(job);

        Job result = jobService.getJobByPublicId("11111111-2222-3333-4444-555555555555");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("uuid.mp3", result.getOriginalFilename());
    }

    /**
     * Bilinmeyen publicId → DAO {@code null} döner → Service {@code null}
     * döner (controller bunu /history redirect'ine çevirir).
     */
    @Test
    public void testGetJobByPublicIdReturnsNullWhenNotExists() {
        when(jobDao.findByPublicId("ghost")).thenReturn(null);
        assertNull(jobService.getJobByPublicId("ghost"));
    }
}
