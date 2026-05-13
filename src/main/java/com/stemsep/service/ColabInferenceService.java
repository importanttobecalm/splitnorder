package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.dao.StemDao;
import com.stemsep.exception.InferenceFailedException;
import com.stemsep.model.Job;
import com.stemsep.model.JobStatus;
import com.stemsep.model.Stem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
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
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kaggle/Colab GPU üzerinde çalışan Demucs Flask servisi ile konuşur.
 *
 * <p>İki adım:
 * <ol>
 *   <li><b>POST /api/separate</b> (multipart/form-data) — orijinal ses dosyasını
 *       Kaggle'a yükler, Demucs blocking olarak ~8 sn'de tamamlar, JSON döner.</li>
 *   <li><b>GET /api/stem/{job_id}/{stem}</b> — 4 stem WAV dosyasını lokal
 *       <code>stems/&lt;jobId&gt;/</code> dizinine indirir.</li>
 * </ol>
 */
@Service
public class ColabInferenceService {

    private static final Logger logger = LoggerFactory.getLogger(ColabInferenceService.class);
    private static final String[] STEMS = {"vocals", "drums", "bass", "other"};

    @Autowired
    private JobDao jobDao;

    @Autowired
    private StemDao stemDao;

    @Value("${colab.api.url:http://localhost:5000}")
    private String colabApiUrl;

    @Value("${stems.directory:stems}")
    private String stemsDirectory;

    @Transactional
    public void processJob(Long jobId) {
        Job job = jobDao.findById(jobId);
        if (job == null) {
            throw new InferenceFailedException("Job bulunamadı: " + jobId);
        }
        job.setStatus(JobStatus.PROCESSING);
        jobDao.update(job);

        try {
            String remoteJobId = callSeparate(job);
            downloadStems(job, remoteJobId);
            createStemRecords(job);
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            jobDao.update(job);
            logger.info("Job {} tamamlandı (Kaggle job_id={})", jobId, remoteJobId);
        } catch (IOException e) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            jobDao.update(job);
            throw new InferenceFailedException("Demucs çağrısı başarısız: " + e.getMessage());
        }
    }

    /**
     * Multipart upload ile orijinal dosyayı Kaggle Flask'a yollar.
     * Cevap JSON: {"status":"completed","job_id":"...","base":"..."}.
     * job_id (veya yoksa fallback) döndürülür.
     */
    private String callSeparate(Job job) throws IOException {
        File audioFile = new File(job.getOriginalFilePath());
        if (!audioFile.exists()) {
            throw new IOException("Orijinal dosya bulunamadı: " + job.getOriginalFilePath());
        }

        String boundary = "----splitnorder" + UUID.randomUUID();
        String remoteJobId = "job-" + job.getPublicId();

        URL url = new URL(colabApiUrl + "/api/separate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("ngrok-skip-browser-warning", "true");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(300000); // Demucs ~8 sn ama 3+ dk şarkı için pay

        try (OutputStream out = conn.getOutputStream()) {
            String lf = "\r\n";
            // job_id alanı
            out.write(("--" + boundary + lf).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"job_id\"" + lf + lf).getBytes(StandardCharsets.UTF_8));
            out.write((remoteJobId + lf).getBytes(StandardCharsets.UTF_8));
            // model alanı
            out.write(("--" + boundary + lf).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"model\"" + lf + lf).getBytes(StandardCharsets.UTF_8));
            out.write((job.getModelUsed() + lf).getBytes(StandardCharsets.UTF_8));
            // file alanı (binary)
            out.write(("--" + boundary + lf).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + audioFile.getName() + "\"" + lf).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: application/octet-stream" + lf + lf).getBytes(StandardCharsets.UTF_8));
            Files.copy(audioFile.toPath(), out);
            out.write((lf + "--" + boundary + "--" + lf).getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            String err = readAll(conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream());
            throw new IOException("Demucs API HTTP " + code + ": " + err);
        }
        String body = readAll(conn.getInputStream());
        logger.info("Demucs separate cevabı: {}", body);
        return remoteJobId;
    }

    /**
     * Her stem için iki format indirir:
     *   GET /api/stem/{job_id}/{stem}?fmt=mp3  → stems/&lt;jobId&gt;/{stem}.mp3
     *   GET /api/stem/{job_id}/{stem}?fmt=wav  → stems/&lt;jobId&gt;/{stem}.wav
     *
     * MP3 arayüz streaming için (hızlı, ~4 MB), WAV indirme için (kayıpsız,
     * ~40 MB). Java'da Stem.filePath MP3'ü işaret eder; WAV path'i convention
     * ile türetilir (uzantı .mp3 → .wav).
     */
    private void downloadStems(Job job, String remoteJobId) throws IOException {
        Path stemsDir = Paths.get(stemsDirectory, job.getPublicId()).toAbsolutePath();
        Files.createDirectories(stemsDir);

        for (String stemType : STEMS) {
            for (String fmt : new String[]{"mp3", "wav"}) {
                URL url = new URL(colabApiUrl + "/api/stem/" + remoteJobId + "/" + stemType + "?fmt=" + fmt);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("ngrok-skip-browser-warning", "true");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(180000); // WAV ~40 MB → bol pay

                int code = conn.getResponseCode();
                if (code != 200) {
                    throw new IOException("Stem indirilemedi (" + stemType + "." + fmt + "): HTTP " + code);
                }
                Path target = stemsDir.resolve(stemType + "." + fmt);
                try (InputStream in = conn.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
                logger.info("Stem indirildi: {} ({} bytes)", target, target.toFile().length());
            }
        }
    }

    private void createStemRecords(Job job) throws IOException {
        Path stemsDir = Paths.get(stemsDirectory, job.getPublicId()).toAbsolutePath();
        Files.createDirectories(stemsDir);
        for (String stemType : STEMS) {
            Path file = stemsDir.resolve(stemType + ".mp3");
            Stem stem = new Stem();
            stem.setJob(job);
            stem.setStemType(stemType);
            stem.setFilePath(file.toString());
            stem.setFileSize(file.toFile().exists() ? file.toFile().length() : 0L);
            stem.setDownloadUrl("/job/" + job.getPublicId() + "/download/" + stemType);
            stemDao.save(stem);
            job.getStems().add(stem);
        }
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
