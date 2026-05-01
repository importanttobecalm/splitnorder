package com.stemsep.controller;

import com.stemsep.model.Job;
import com.stemsep.model.Stem;
import com.stemsep.service.JobService;
import com.stemsep.service.StemService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
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

        Map<String, String> stemUrls = new HashMap<>();
        for (Stem s : job.getStems()) {
            stemUrls.put(s.getStemType(), s.getDownloadUrl());
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

    @GetMapping("/{id}/download/{stemType}")
    public void downloadStem(@PathVariable Long id, @PathVariable String stemType,
                             HttpServletResponse response) throws IOException {
        Stem stem = stemService.getStemByJobAndType(id, stemType);
        if (stem == null || stem.getFilePath() == null) {
            response.sendError(404);
            return;
        }

        File file = new File(stem.getFilePath());
        if (!file.exists()) {
            response.sendError(404);
            return;
        }

        response.setContentType("audio/wav");
        response.setHeader("Content-Disposition", "inline; filename=\"" + stemType + ".wav\"");
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
                try (InputStream is = new URL(url).openStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        zos.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    logger.error("Failed to fetch stem {} from {}: {}", stem.getStemType(), url, e.getMessage());
                }
                zos.closeEntry();
            }
        }
    }
}
