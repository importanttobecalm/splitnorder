package com.stemsep.controller;

import com.stemsep.model.Job;
import com.stemsep.model.Stem;
import com.stemsep.service.JobService;
import com.stemsep.service.StemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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

    @GetMapping("/{id}")
    public String showJob(@PathVariable Long id, Model model) {
        Job job = jobService.getJob(id);
        if (job == null) {
            return "redirect:/";
        }

        model.addAttribute("job", job);

        // Tarayıcı için local proxy URL'leri oluştur (ngrok doğrudan açılmaz)
        Map<String, String> stemUrls = new HashMap<>();
        for (Stem s : job.getStems()) {
            stemUrls.put(s.getStemType(), "/job/" + id + "/download/" + s.getStemType());
        }
        model.addAttribute("stemUrls", stemUrls);

        switch (job.getStatus()) {
            case PENDING:
            case PROCESSING:
                return "processing";
            case COMPLETED:
                return "result";
            case FAILED:
                model.addAttribute("error", job.getErrorMessage());
                return "result";
            default:
                return "redirect:/";
        }
    }

    @GetMapping("/{id}/status")
    @ResponseBody
    public Map<String, Object> getJobStatus(@PathVariable Long id) {
        Job job = jobService.getJob(id);
        Map<String, Object> response = new HashMap<>();

        if (job == null) {
            response.put("error", "Job not found");
            return response;
        }

        response.put("id", job.getId());
        response.put("status", job.getStatus().name());
        response.put("filename", job.getOriginalFilename());

        return response;
    }

    /**
     * Stem proxy endpoint — ngrok üzerindeki Kaggle API'den stem'i çekip
     * tarayıcıya döndürür. ngrok-skip-browser-warning header'ı eklenir.
     * Range header desteği ile audio player'da seek çalışır.
     */
    @GetMapping("/{id}/download/{stemType}")
    public void downloadStem(@PathVariable Long id, @PathVariable String stemType,
                             HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        Stem stem = stemService.getStemByJobAndType(id, stemType);
        if (stem == null || stem.getDownloadUrl() == null) {
            response.sendError(404);
            return;
        }

        String remoteUrl = stem.getDownloadUrl();
        logger.info("[PROXY] job={} stem={} → {}", id, stemType, remoteUrl);

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(remoteUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("ngrok-skip-browser-warning", "true");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);

            // Range header'ı ilet (audio seek desteği)
            String rangeHeader = request.getHeader("Range");
            if (rangeHeader != null) {
                conn.setRequestProperty("Range", rangeHeader);
            }

            int status = conn.getResponseCode();
            response.setStatus(status);
            response.setContentType("audio/wav");
            response.setHeader("Content-Disposition", "inline; filename=\"" + stemType + ".wav\"");
            response.setHeader("Accept-Ranges", "bytes");

            // Content-Length ve Content-Range header'larını kopyala
            String contentLength = conn.getHeaderField("Content-Length");
            if (contentLength != null) {
                response.setHeader("Content-Length", contentLength);
            }
            String contentRange = conn.getHeaderField("Content-Range");
            if (contentRange != null) {
                response.setHeader("Content-Range", contentRange);
            }

            try (InputStream is = conn.getInputStream();
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[16384];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            logger.error("[PROXY] Failed to fetch stem {} for job {}: {}", stemType, id, e.getMessage());
            response.sendError(502, "GPU API'den stem alınamadı");
        }
    }

    @GetMapping("/{id}/download-all")
    public void downloadAll(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Job job = jobService.getJob(id);
        if (job == null) {
            response.sendError(404);
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"stems_" + job.getOriginalFilename() + ".zip\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Stem stem : job.getStems()) {
                String url = stem.getDownloadUrl();
                if (url == null || !url.startsWith("http")) {
                    logger.warn("Skipping stem {} for job {}: no remote URL", stem.getStemType(), id);
                    continue;
                }
                zos.putNextEntry(new ZipEntry(stem.getStemType() + ".wav"));
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestProperty("ngrok-skip-browser-warning", "true");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(120000);
                    try (InputStream is = conn.getInputStream()) {
                        byte[] buffer = new byte[16384];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Failed to fetch stem {} from {}: {}", stem.getStemType(), url, e.getMessage());
                }
                zos.closeEntry();
            }
        }
    }
}
