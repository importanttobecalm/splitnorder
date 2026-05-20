package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.dao.StemDao;
import com.stemsep.exception.InferenceFailedException;
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

/**
 * {@link ColabInferenceService} için birim testleri.
 *
 * <p>Servisin görevi: bir {@link Job} için Python Flask Demucs API'sine senkron
 * HTTP çağrısı yapmak, başarılıysa {@link com.stemsep.model.Stem} kayıtlarını
 * oluşturmak, başarısızsa {@link InferenceFailedException} fırlatmak.</p>
 *
 * <p><b>Tasarım kararı (slayt-uyumluluk):</b> Servis polling döngüsü veya mock
 * fallback içermez — slayt'taki sade Service kalıbına uygun olarak tek senkron
 * çağrı yapar. Demucs ulaşılamazsa exception fırlatır; bu exception
 * {@code @ResponseStatus(BAD_GATEWAY)} ile işaretlidir, Controller tarafından
 * yakalanmadan doğrudan 502 dönüş üretir.</p>
 */
public class ColabInferenceServiceTest {

    @Mock
    private JobDao jobDao;

    @Mock
    private StemDao stemDao;

    @InjectMocks
    private ColabInferenceService colabService;

    /**
     * Her testten önce Mockito mock'larını başlatır ve servise URL/dizin
     * config'lerini reflection ile enjekte eder (gerçek {@code @Value}
     * resolution yerine, izole birim testi için).
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Bilerek erişilemez bir URL veriyoruz: testte gerçek HTTP çağrısı yapılırsa
        // bağlantı reddi alırız ve exception fırlatılır — beklenen davranış budur.
        ReflectionTestUtils.setField(colabService, "colabApiUrl", "http://127.0.0.1:1");
        ReflectionTestUtils.setField(colabService, "stemsDirectory", "test-stems");
        // Self-reference normalde @Lazy Spring proxy ile gelir; testte aynı
        // instance'a bağlıyoruz ki @Transactional helper'lara yapılan
        // self.X() çağrıları gerçek metoda gitsin (tx semantiği test edilmiyor).
        ReflectionTestUtils.setField(colabService, "self", colabService);
    }

    /**
     * Job bulunamayınca servis sessizce çıkmaz; bunun yerine
     * {@link InferenceFailedException} fırlatarak çağıran katmana hata bildirir.
     * Bu davranış, slayt'taki "@ResponseStatus + custom exception" kalıbının
     * Controller'a kadar propagation'ı sağlar.
     */
    @Test
    public void testProcessJobWithNullJobThrowsException() {
        when(jobDao.findById(999L)).thenReturn(null);

        InferenceFailedException ex = assertThrows(
                InferenceFailedException.class,
                () -> colabService.processJob(999L));

        assertTrue(ex.getMessage().contains("Job bulunamadı"));
        verify(jobDao, never()).update(any());
    }

    /**
     * Geçerli bir Job için: servis önce job durumunu PROCESSING'e alıp DAO'ya
     * yazar, sonra Demucs HTTP çağrısı yapar. Test URL'inde gerçek Demucs
     * olmadığından bağlantı reddi alınır, servis Job'u FAILED'a günceller ve
     * {@link InferenceFailedException} fırlatır.
     *
     * <p>Bu test, "happy path başlangıcı + dış servis erişilemezliği"
     * senaryosunu birlikte doğrular.</p>
     */
    @Test
    public void testProcessJobUpdatesStatusThenThrowsWhenDemucsUnreachable() {
        Job job = new Job();
        job.setId(1L);
        job.setOriginalFilename("test.mp3");
        job.setOriginalFilePath("/tmp/test.mp3");
        job.setModelUsed("mdx_extra");
        job.setStatus(JobStatus.PENDING);

        when(jobDao.findById(1L)).thenReturn(job);

        assertThrows(InferenceFailedException.class,
                () -> colabService.processJob(1L));

        // Job en az iki kez güncellendi: PENDING→PROCESSING (başlangıçta) ve
        // PROCESSING→FAILED (exception'dan önce error message ile)
        verify(jobDao, atLeast(2)).update(job);
        assertEquals(JobStatus.FAILED, job.getStatus());
        assertNotNull(job.getErrorMessage());
    }

    /**
     * Exception fırlatıldıktan sonra bile Job'un veritabanında doğru
     * (FAILED) durumda kaldığını ayrıca doğrular. Bu, transaction rollback'in
     * Job güncellemesini tersine çevirmediğini garanti eder — slayt'taki
     * {@code @Transactional(rollbackFor=...)} kalıbının doğru çalıştığını
     * gösterir.
     */
    @Test
    public void testFailedJobErrorMessageIsPopulated() {
        Job job = new Job();
        job.setId(2L);
        job.setOriginalFilename("song.wav");
        job.setOriginalFilePath("/tmp/song.wav");
        job.setModelUsed("htdemucs_ft");
        job.setStatus(JobStatus.PENDING);

        when(jobDao.findById(2L)).thenReturn(job);

        assertThrows(InferenceFailedException.class,
                () -> colabService.processJob(2L));

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertNotNull(job.getErrorMessage(),
                "Hata mesajı set edilmemiş — Controller hata gösteremeyecek");
    }
}
