package com.stemsep.dao;

import com.stemsep.model.Job;
import com.stemsep.model.User;
import com.stemsep.model.JobStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link JobDao} için izole birim testleri.
 *
 * <p>Job (ayrıştırma görevi) varlığı, projenin merkezi entity'sidir; user'a
 * {@code @ManyToOne} ile bağlanır, status alanı {@link JobStatus} enum ile
 * temsil edilir. Bu testler DAO katmanının Job'u doğru persist/load/update
 * ettiğini ve {@code @ManyToOne} ilişkisinin Job → User yönünde set
 * edilebildiğini doğrular.</p>
 *
 * <p><b>Slayt referansı:</b> "Hibernate Entity (II) — @ManyToOne" bölümü.</p>
 */
public class JobDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @InjectMocks
    private JobDao jobDao;

    /** Her test öncesi mock SessionFactory → mock Session bağlantısı kurulur. */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    /**
     * Yeni bir Job'un INSERT işlemine girdiğini ve {@code @ManyToOne} ilişkisi
     * olan User foreign key'inin korunduğunu doğrular.
     */
    @Test
    public void testSaveJob() {
        User user = new User();
        user.setId(1L);

        Job job = new Job();
        job.setUser(user);
        job.setOriginalFilename("music.mp3");
        job.setModelUsed("mdx_extra");
        job.setStatus(JobStatus.PENDING);

        jobDao.save(job);

        verify(session).persist(job);
    }

    /**
     * Var olan bir Job'un ID ile getirilmesinde dosya adı ve durum gibi
     * temel alanların doğru gelmesi.
     */
    @Test
    public void testFindByIdReturnsJob() {
        Job job = new Job();
        job.setId(1L);
        job.setOriginalFilename("song.wav");
        job.setStatus(JobStatus.COMPLETED);

        when(session.get(Job.class, 1L)).thenReturn(job);

        Job result = jobDao.findById(1L);

        assertNotNull(result);
        assertEquals("song.wav", result.getOriginalFilename());
        assertEquals(JobStatus.COMPLETED, result.getStatus());
    }

    /** Olmayan bir Job ID için {@code null} dönüşü (silinmiş/iptal edilmiş job senaryosu). */
    @Test
    public void testFindByIdReturnsNullWhenNotFound() {
        when(session.get(Job.class, 999L)).thenReturn(null);

        Job result = jobDao.findById(999L);

        assertNull(result);
    }

    /**
     * Job durumunun PROCESSING'e güncellenmesi gibi UPDATE senaryosu —
     * {@code Session.merge()} çağrılır.
     */
    @Test
    public void testUpdateJob() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus(JobStatus.PROCESSING);

        jobDao.update(job);

        verify(session).merge(job);
    }

    /**
     * Job silme — kullanıcı geçmişten kayıt silebilir; bu durumda
     * {@code Session.remove()} tetiklenir.
     */
    @Test
    public void testDeleteJob() {
        Job job = new Job();
        job.setId(1L);

        jobDao.delete(job);

        verify(session).remove(job);
    }
}
