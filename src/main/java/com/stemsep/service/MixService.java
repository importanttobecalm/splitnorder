package com.stemsep.service;

import com.stemsep.dao.MixedTrackDao;
import com.stemsep.exception.InferenceFailedException;
import com.stemsep.exception.JobNotFoundException;
import com.stemsep.exception.StorageQuotaExceededException;
import com.stemsep.exception.UnauthorizedJobAccessException;
import com.stemsep.exception.UploadValidationException;
import com.stemsep.exception.ErrorCode;
import com.stemsep.model.Job;
import com.stemsep.model.MixedTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Karma mix üretimi: kullanıcının seçtiği stem alt kümesini (örn.
 * "vocals,drums") Kaggle Flask {@code /api/mix} endpoint'inde birleştirir,
 * dönen dosyayı diske kaydeder ve {@link MixedTrack} DB kaydı oluşturur.
 *
 * <p>Pattern olarak {@link ColabInferenceService} ile aynı: HTTP üzerinden
 * Kaggle Flask'a istek atar, dosya stream'i geri çeker, FS + DB persist
 * eder. ADR-13 gerekçeli — slayt-uyumlu mevcut pattern'in genişlemesi.</p>
 */
@Service
public class MixService {

    private static final Logger logger = LoggerFactory.getLogger(MixService.class);
    private static final Set<String> ALLOWED_STEMS =
            new LinkedHashSet<>(Arrays.asList("vocals", "drums", "bass", "other"));
    private static final Set<String> ALLOWED_FORMATS =
            new LinkedHashSet<>(Arrays.asList("mp3", "wav"));

    @Autowired
    private com.stemsep.dao.JobDao jobDao;

    @Autowired
    private MixedTrackDao mixedTrackDao;

    @Autowired
    private StorageQuotaService quotaService;

    @Value("${colab.api.url:http://localhost:5000}")
    private String colabApiUrl;

    @Value("${stems.directory:stems}")
    private String stemsDirectory;

    /**
     * Yeni bir karma mix üretir ve kaydeder.
     *
     * @param jobPublicId  kaynak Job'un publicId'si
     * @param userId       isteği yapan user (yetki kontrolü)
     * @param stems        birleştirilecek stem türleri (en az 2, en fazla 4)
     * @param format       "mp3" | "wav"
     * @return kaydedilen MixedTrack
     */
    @Transactional
    public MixedTrack createMix(String jobPublicId, Long userId, List<String> stems, String format) throws IOException {
        Job job = jobDao.findByPublicId(jobPublicId);
        if (job == null) {
            throw new JobNotFoundException("Job bulunamadı: " + jobPublicId);
        }
        if (!job.getUser().getId().equals(userId)) {
            throw new UnauthorizedJobAccessException(userId, jobPublicId);
        }

        validateRequest(stems, format);

        // Kota: çıktı boyutu kaynak stem'lerin ortalaması civarı; tahmin için
        // en büyük stem boyutunu kullanırız (üst sınır olarak güvenli).
        long estimatedBytes = job.getStems().stream()
                .filter(s -> stems.contains(s.getStemType()))
                .mapToLong(s -> s.getFileSize() != null ? s.getFileSize() : 0L)
                .max().orElse(0L);
        if (quotaService.wouldExceed(userId, estimatedBytes)) {
            throw new StorageQuotaExceededException(
                    "Mix üretimi için yer yok (kullanıcı=" + userId + ")");
        }

        String publicId = UUID.randomUUID().toString();
        Path mixDir = Paths.get(stemsDirectory, job.getPublicId(), "mixes").toAbsolutePath();
        Files.createDirectories(mixDir);
        Path mixFile = mixDir.resolve(publicId + "." + format);

        downloadMix(job.getPublicId(), stems, format, mixFile);

        long size = Files.size(mixFile);
        MixedTrack track = new MixedTrack();
        track.setPublicId(publicId);
        track.setJob(job);
        track.setName(buildMixName(stems));
        track.setStemTypes(String.join(",", stems));
        track.setFilePath(mixFile.toString());
        track.setFileSize(size);
        track.setFormat(format);

        mixedTrackDao.save(track);
        logger.info("Mix kaydedildi: publicId={}, job={}, stems={}, size={}",
                publicId, jobPublicId, track.getStemTypes(), size);
        return track;
    }

    @Transactional
    public void deleteMix(String mixPublicId, Long userId) throws IOException {
        MixedTrack mix = mixedTrackDao.findByPublicId(mixPublicId);
        if (mix == null) {
            throw new JobNotFoundException("Mix bulunamadı: " + mixPublicId);
        }
        if (!mix.getJob().getUser().getId().equals(userId)) {
            throw new UnauthorizedJobAccessException(userId, mixPublicId);
        }
        Files.deleteIfExists(Paths.get(mix.getFilePath()));
        mixedTrackDao.delete(mix);
        logger.info("Mix silindi: publicId={}, userId={}", mixPublicId, userId);
    }

    @Transactional(readOnly = true)
    public List<MixedTrack> listForJob(String jobPublicId, Long userId) {
        Job job = jobDao.findByPublicId(jobPublicId);
        if (job == null) {
            throw new JobNotFoundException("Job bulunamadı: " + jobPublicId);
        }
        if (!job.getUser().getId().equals(userId)) {
            throw new UnauthorizedJobAccessException(userId, jobPublicId);
        }
        return mixedTrackDao.findByJobId(job.getId());
    }

    @Transactional(readOnly = true)
    public MixedTrack getForDownload(String mixPublicId, Long userId) {
        MixedTrack mix = mixedTrackDao.findByPublicId(mixPublicId);
        if (mix == null) {
            throw new JobNotFoundException("Mix bulunamadı: " + mixPublicId);
        }
        if (!mix.getJob().getUser().getId().equals(userId)) {
            throw new UnauthorizedJobAccessException(userId, mixPublicId);
        }
        return mix;
    }

    private void validateRequest(List<String> stems, String format) {
        if (stems == null || stems.size() < 2) {
            throw new UploadValidationException(ErrorCode.UPLOAD_INVALID_FORMAT,
                    "Mix için en az 2 stem seçilmeli");
        }
        if (stems.size() > ALLOWED_STEMS.size()) {
            throw new UploadValidationException(ErrorCode.UPLOAD_INVALID_FORMAT,
                    "Mix için en fazla " + ALLOWED_STEMS.size() + " stem seçilebilir");
        }
        for (String s : stems) {
            if (!ALLOWED_STEMS.contains(s)) {
                throw new UploadValidationException(ErrorCode.UPLOAD_INVALID_FORMAT,
                        "Geçersiz stem türü: " + s);
            }
        }
        if (!ALLOWED_FORMATS.contains(format)) {
            throw new UploadValidationException(ErrorCode.UPLOAD_INVALID_FORMAT,
                    "Geçersiz format: " + format);
        }
    }

    private String buildMixName(List<String> stems) {
        // "vocals + drums" — UI'da gösterim için
        return stems.stream()
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" + "));
    }

    /**
     * Kaggle Flask {@code POST /api/mix} (JSON body) → ham audio stream
     * (mp3/wav). Pattern {@link ColabInferenceService#callSeparate} ile
     * aynı (HttpURLConnection — slayt-dışı ama proje genelinde tutarlı).
     */
    private void downloadMix(String jobPublicId, List<String> stems, String format, Path target) throws IOException {
        String body = buildJsonBody(jobPublicId, stems, format);
        URL url = new URL(colabApiUrl + "/api/mix");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("ngrok-skip-browser-warning", "true");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(180000);

        try (OutputStream out = conn.getOutputStream()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            String err = readAll(conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream());
            throw new InferenceFailedException("Mix API HTTP " + code + ": " + err);
        }

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String buildJsonBody(String jobPublicId, List<String> stems, String format) {
        String stemsJson = stems.stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(","));
        return "{\"job_id\":\"job-" + jobPublicId + "\","
             + "\"stems\":[" + stemsJson + "],"
             + "\"fmt\":\"" + format + "\"}";
    }

    private String readAll(InputStream in) throws IOException {
        if (in == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString().trim();
        }
    }
}
