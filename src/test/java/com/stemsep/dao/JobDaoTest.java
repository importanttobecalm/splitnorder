package com.stemsep.dao;

import com.stemsep.model.Job;
import com.stemsep.model.JobStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JobDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @InjectMocks
    private JobDao jobDao;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    public void testSaveJob() {
        Job job = new Job();
        job.setSessionId("sess123");
        job.setOriginalFilename("music.mp3");
        job.setModelUsed("mdx_extra");
        job.setStatus(JobStatus.PENDING);

        jobDao.save(job);

        verify(session).persist(job);
    }

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

    @Test
    public void testFindByIdReturnsNullWhenNotFound() {
        when(session.get(Job.class, 999L)).thenReturn(null);

        Job result = jobDao.findById(999L);

        assertNull(result);
    }

    @Test
    public void testUpdateJob() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus(JobStatus.PROCESSING);

        jobDao.update(job);

        verify(session).merge(job);
    }

    @Test
    public void testDeleteJob() {
        Job job = new Job();
        job.setId(1L);

        jobDao.delete(job);

        verify(session).remove(job);
    }
}
