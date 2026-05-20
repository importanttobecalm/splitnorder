package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.dao.MixedTrackDao;
import com.stemsep.exception.JobNotFoundException;
import com.stemsep.exception.StorageQuotaExceededException;
import com.stemsep.exception.UnauthorizedJobAccessException;
import com.stemsep.exception.UploadValidationException;
import com.stemsep.model.Job;
import com.stemsep.model.MixedTrack;
import com.stemsep.model.Stem;
import com.stemsep.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * {@link MixService} davranış testleri (Flask {@code /api/mix} HTTP çağrısı
 * yapılmadan — validasyon, yetki ve kota guard'larını izole doğrular).
 *
 * <p>Flask çağrısının kendisini test etmiyor; o entegrasyon testidir
 * (Kaggle live URL gerektirir). Burada Service'in iş kuralları doğrulanır.</p>
 */
public class MixServiceTest {

    @Mock private JobDao jobDao;
    @Mock private MixedTrackDao mixedTrackDao;
    @Mock private StorageQuotaService quotaService;

    @InjectMocks private MixService service;

    private Job job;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        User u = new User();
        u.setId(1L);
        job = new Job();
        job.setId(10L);
        job.setPublicId("job-pub");
        job.setUser(u);
        // 4 stem mock — boyutlar 1 MB civarı
        for (String s : Arrays.asList("vocals", "drums", "bass", "other")) {
            Stem st = new Stem();
            st.setStemType(s);
            st.setFileSize(1024L * 1024L);
            job.getStems().add(st);
        }
    }

    @Test
    public void createMix_throwsWhenJobMissing() {
        when(jobDao.findByPublicId("ghost")).thenReturn(null);
        assertThrows(JobNotFoundException.class,
                () -> service.createMix("ghost", 1L, Arrays.asList("vocals", "drums"), "mp3"));
    }

    @Test
    public void createMix_throwsWhenUserNotOwner() {
        when(jobDao.findByPublicId("job-pub")).thenReturn(job);
        assertThrows(UnauthorizedJobAccessException.class,
                () -> service.createMix("job-pub", 999L, Arrays.asList("vocals", "drums"), "mp3"));
    }

    @Test
    public void createMix_throwsWhenLessThanTwoStems() {
        when(jobDao.findByPublicId("job-pub")).thenReturn(job);
        assertThrows(UploadValidationException.class,
                () -> service.createMix("job-pub", 1L, List.of("vocals"), "mp3"));
    }

    @Test
    public void createMix_throwsOnInvalidStemName() {
        when(jobDao.findByPublicId("job-pub")).thenReturn(job);
        assertThrows(UploadValidationException.class,
                () -> service.createMix("job-pub", 1L, Arrays.asList("vocals", "guitar"), "mp3"));
    }

    @Test
    public void createMix_throwsOnInvalidFormat() {
        when(jobDao.findByPublicId("job-pub")).thenReturn(job);
        assertThrows(UploadValidationException.class,
                () -> service.createMix("job-pub", 1L, Arrays.asList("vocals", "drums"), "flac"));
    }

    @Test
    public void createMix_throwsWhenQuotaExceeded() {
        when(jobDao.findByPublicId("job-pub")).thenReturn(job);
        when(quotaService.wouldExceed(eq(1L), anyLong())).thenReturn(true);
        assertThrows(StorageQuotaExceededException.class,
                () -> service.createMix("job-pub", 1L, Arrays.asList("vocals", "drums"), "mp3"));
    }

    @Test
    public void deleteMix_throwsWhenMixNotFound() {
        when(mixedTrackDao.findByPublicId("ghost")).thenReturn(null);
        assertThrows(JobNotFoundException.class,
                () -> service.deleteMix("ghost", 1L));
    }

    @Test
    public void deleteMix_throwsWhenUserNotOwner() {
        MixedTrack m = new MixedTrack();
        m.setPublicId("mix-1");
        m.setJob(job);
        m.setFilePath("/tmp/nonexistent.mp3");
        when(mixedTrackDao.findByPublicId("mix-1")).thenReturn(m);

        assertThrows(UnauthorizedJobAccessException.class,
                () -> service.deleteMix("mix-1", 999L));
        verify(mixedTrackDao, never()).delete(any());
    }

    @Test
    public void deleteMix_happyPath_callsDaoDelete() throws IOException {
        MixedTrack m = new MixedTrack();
        m.setPublicId("mix-1");
        m.setJob(job);
        m.setFilePath("/tmp/will-not-exist-" + System.nanoTime() + ".mp3");
        when(mixedTrackDao.findByPublicId("mix-1")).thenReturn(m);

        service.deleteMix("mix-1", 1L);
        verify(mixedTrackDao).delete(m);
    }

    @Test
    public void listForJob_throwsWhenJobMissing() {
        when(jobDao.findByPublicId("ghost")).thenReturn(null);
        assertThrows(JobNotFoundException.class,
                () -> service.listForJob("ghost", 1L));
    }

    @Test
    public void listForJob_returnsDaoResult() {
        when(jobDao.findByPublicId("job-pub")).thenReturn(job);
        MixedTrack m = new MixedTrack();
        when(mixedTrackDao.findByJobId(10L)).thenReturn(List.of(m));
        List<MixedTrack> out = service.listForJob("job-pub", 1L);
        assertEquals(1, out.size());
    }

    private static <T> T eq(T value) { return org.mockito.ArgumentMatchers.eq(value); }
}
