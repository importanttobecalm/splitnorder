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

    @Test
    public void testGetJobReturnsNullWhenNotExists() {
        when(jobDao.findById(999L)).thenReturn(null);

        Job result = jobService.getJob(999L);
        assertNull(result);
    }

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

    @Test
    public void testUpdateJobStatusNullJob() {
        when(jobDao.findById(999L)).thenReturn(null);

        jobService.updateJobStatus(999L, JobStatus.COMPLETED);

        verify(jobDao, never()).update(any());
    }
}
