package com.stemsep.controller;

import com.stemsep.model.Job;
import com.stemsep.model.User;
import com.stemsep.model.Stem;
import com.stemsep.service.ColabInferenceService;
import com.stemsep.service.JobService;
import com.stemsep.service.StemService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/job")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private JobService jobService;

    @Autowired
    private StemService stemService;

    @Autowired
    private ColabInferenceService colabService;

    @GetMapping({"/{publicId}", "/{publicId}/result"})
    public String showJob(@PathVariable String publicId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        Job job = jobService.getJobByPublicId(publicId);
        if (job == null || !job.getUser().getId().equals(user.getId())) {
            return "redirect:/history";
        }

        // Tüm durumları studio'ya yönlendir — HomeController jobStatus'a göre
        // doğru overlay'i (processing / audio engine / error) gösterir.
        return "redirect:/?jobId=" + job.getPublicId();
    }

    @GetMapping("/{publicId}/status")
    @ResponseBody
    public Map<String, Object> getJobStatus(@PathVariable String publicId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Job job = jobService.getJobByPublicId(publicId);
        Map<String, Object> response = new HashMap<>();

        if (job == null || user == null || !job.getUser().getId().equals(user.getId())) {
            response.put("error", "Job not found or access denied");
            return response;
        }

        response.put("id", job.getPublicId());
        response.put("status", job.getStatus().name());
        response.put("filename", job.getOriginalFilename());

        return response;
    }

    @GetMapping("/{publicId}/stream/{stemType}")
    public void streamStem(@PathVariable String publicId, @PathVariable String stemType,
                           @RequestParam(value = "fmt", required = false) String fmt,
                           HttpSession session, HttpServletResponse response) throws IOException {
        User user = (User) session.getAttribute("user");
        Job job = jobService.getJobByPublicId(publicId);

        if (job == null || user == null || !job.getUser().getId().equals(user.getId())) {
            response.sendError(403);
            return;
        }

        File file;
        String contentType;
        if ("original".equals(stemType)) {
            if (job.getOriginalFilePath() == null) {
                response.sendError(404);
                return;
            }
            file = new File(job.getOriginalFilePath());
            contentType = "audio/mpeg";
        } else {
            Stem stem = stemService.getStemByJobAndType(job.getId(), stemType);
            if (stem == null || stem.getFilePath() == null) {
                response.sendError(404);
                return;
            }
            // fmt query'si verilirse uzantıyı değiştirip alternatif dosyaya bak.
            // Stem.filePath default MP3 — fmt=wav ile WAV master servis edilir.
            String basePath = stem.getFilePath();
            if ("wav".equalsIgnoreCase(fmt)) {
                String alt = basePath.replaceAll("\\.(mp3|wav|flac)$", ".wav");
                File altFile = new File(alt);
                if (!altFile.exists()) {
                    // Lazy WAV: processing aşamasında sadece MP3 indirildi;
                    // ilk WAV talebinde Kaggle'dan çekiyoruz.
                    try {
                        colabService.ensureWavAvailable(job.getId(), stemType);
                    } catch (IOException e) {
                        logger.warn("Lazy WAV fetch başarısız ({}): {}", stemType, e.getMessage());
                    }
                }
                file = altFile.exists() ? altFile : new File(basePath);
            } else if ("mp3".equalsIgnoreCase(fmt)) {
                String alt = basePath.replaceAll("\\.(mp3|wav|flac)$", ".mp3");
                File altFile = new File(alt);
                if (altFile.exists()) {
                    file = altFile;
                } else {
                    file = new File(basePath);
                }
            } else {
                file = new File(basePath);
            }
            String lower = file.getName().toLowerCase();
            if (lower.endsWith(".mp3")) {
                contentType = "audio/mpeg";
            } else if (lower.endsWith(".flac")) {
                contentType = "audio/flac";
            } else {
                contentType = "audio/wav";
            }
        }

        if (!file.exists()) {
            response.sendError(404);
            return;
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        response.setHeader("Accept-Ranges", "bytes");
        response.setContentLengthLong(file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    @GetMapping("/{publicId}/download/{stemType}")
    public void downloadStem(@PathVariable String publicId, @PathVariable String stemType,
                             HttpSession session, HttpServletResponse response) throws IOException {
        User user = (User) session.getAttribute("user");
        Job job = jobService.getJobByPublicId(publicId);

        if (job == null || user == null || !job.getUser().getId().equals(user.getId())) {
            response.sendError(403);
            return;
        }

        Stem stem = stemService.getStemByJobAndType(job.getId(), stemType);
        if (stem == null || stem.getFilePath() == null) {
            response.sendError(404);
            return;
        }

        File file = new File(stem.getFilePath());
        if (!file.exists()) {
            response.sendError(404);
            return;
        }

        // Uzantıya göre contentType + filename (eski .wav'lar da çalışsın)
        String lower = file.getName().toLowerCase();
        String ext = lower.endsWith(".mp3") ? ".mp3"
                   : lower.endsWith(".flac") ? ".flac" : ".wav";
        String ct  = ext.equals(".mp3")  ? "audio/mpeg"
                   : ext.equals(".flac") ? "audio/flac" : "audio/wav";
        response.setContentType(ct);
        response.setHeader("Content-Disposition", "inline; filename=\"" + stemType + ext + "\"");
        response.setContentLength((int) file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    @GetMapping("/{publicId}/download-all")
    public void downloadAll(@PathVariable String publicId,
                            @RequestParam(value = "format", required = false) String format,
                            HttpSession session, HttpServletResponse response) throws IOException {
        User user = (User) session.getAttribute("user");
        Job job = jobService.getJobByPublicId(publicId);

        if (job == null || user == null || !job.getUser().getId().equals(user.getId())) {
            response.sendError(403);
            return;
        }

        // Default mp3 — küçük ZIP, hızlı. format=wav ile kayıpsız master.
        final String fmt = "wav".equalsIgnoreCase(format) ? "wav" : "mp3";

        if ("wav".equals(fmt)) {
            try {
                colabService.ensureAllWavsAvailable(job.getId());
            } catch (IOException e) {
                logger.warn("Lazy WAV fetch (download-all) başarısız: {}", e.getMessage());
            }
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"stems_" + fmt + "_" + job.getOriginalFilename() + ".zip\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Stem stem : job.getStems()) {
                // Stem.filePath default uzantısından format'a göre dosya seç
                String basePath = stem.getFilePath();
                String resolved = basePath.replaceAll("\\.(mp3|wav|flac)$", "." + fmt);
                File file = new File(resolved);
                // Format yoksa fallback orijinal filePath'e (eski .wav-only jobs)
                if (!file.exists()) {
                    file = new File(basePath);
                }
                if (file.exists()) {
                    String name = file.getName().toLowerCase();
                    String zext = name.endsWith(".mp3") ? ".mp3"
                                : name.endsWith(".flac") ? ".flac" : ".wav";
                    zos.putNextEntry(new ZipEntry(stem.getStemType() + zext));
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }
                    zos.closeEntry();
                }
            }
        }
    }
}
