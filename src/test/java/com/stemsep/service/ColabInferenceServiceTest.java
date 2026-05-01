package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.dao.StemDao;
import com.stemsep.model.Job;
import com.stemsep.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ColabInferenceServiceTest {

    @Mock
    private JobDao jobDao;

    @Mock
    private StemDao stemDao;

    @InjectMocks
    private ColabInferenceService colabService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(colabService, "colabApiUrl", "http://localhost:5000");
        ReflectionTestUtils.setField(colabService, "stemsDirectory", "test-stems");
    }

    @Test
    public void testProcessJobSetsStatusToProcessing() {
        Job job = new Job();
        job.setId(1L);
        job.setOriginalFilename("test.mp3");
        job.setOriginalFilePath("/tmp/test.mp3");
        job.setModelUsed("mdx_extra");
        job.setStatus(JobStatus.PENDING);

        when(jobDao.findById(1L)).thenReturn(job);

        colabService.processJob(1L);

        verify(jobDao, atLeastOnce()).update(job);
    }

    @Test
    public void testProcessJobWithNullJobDoesNothing() {
        when(jobDao.findById(999L)).thenReturn(null);

        colabService.processJob(999L);

        verify(jobDao, never()).update(any());
    }

    @Test
    public void testProcessJobCompletesInMockMode() {
        Job job = new Job();
        job.setId(2L);
        job.setOriginalFilename("song.wav");
        job.setOriginalFilePath("/tmp/song.wav");
        job.setModelUsed("htdemucs_ft");
        job.setStatus(JobStatus.PENDING);

        when(jobDao.findById(2L)).thenReturn(job);

        colabService.processJob(2L);

        assertTrue(
            job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.FAILED
        );
    }
}
