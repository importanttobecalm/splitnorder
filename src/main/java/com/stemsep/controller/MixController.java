package com.stemsep.controller;

import com.stemsep.exception.StorageQuotaExceededException;
import com.stemsep.exception.UploadValidationException;
import com.stemsep.model.MixedTrack;
import com.stemsep.model.User;
import com.stemsep.service.MixService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Karma mix endpoint'leri — kullanıcı seçtiği stem alt kümesinden yeni bir
 * parça üretip indirir, eski mix'leri listeler/siler.
 *
 * <p>Pattern olarak {@link JobController}'a paralel: studio'dan AJAX
 * çağrılır, {@code @ResponseBody} JSON döner; indirme klasik streaming.</p>
 */
@Controller
@RequestMapping("/job/{publicId}/mix")
public class MixController {

    private static final Logger logger = LoggerFactory.getLogger(MixController.class);

    @Autowired
    private MixService mixService;

    /** Yeni mix üret. Body örneği: {@code stems=vocals,drums&fmt=mp3}. */
    @PostMapping
    @ResponseBody
    public Map<String, Object> createMix(@PathVariable String publicId,
                                         @RequestParam("stems") String stemsCsv,
                                         @RequestParam(value = "fmt", defaultValue = "mp3") String fmt,
                                         HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("error", "UNAUTHORIZED");
            return response;
        }

        List<String> stems = Arrays.stream(stemsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        try {
            MixedTrack mix = mixService.createMix(publicId, user.getId(), stems, fmt);
            response.put("mixId", mix.getPublicId());
            response.put("name", mix.getName());
            response.put("stemTypes", mix.getStemTypes());
            response.put("format", mix.getFormat());
            response.put("fileSize", mix.getFileSize());
            response.put("downloadUrl", "/job/" + publicId + "/mix/" + mix.getPublicId() + "/download");
            return response;
        } catch (StorageQuotaExceededException e) {
            response.put("error", "storage.error.quotaExceeded");
            return response;
        } catch (UploadValidationException e) {
            response.put("error", e.getCode().getMessageKey());
            return response;
        } catch (IOException e) {
            logger.error("Mix create IOException: {}", e.getMessage(), e);
            response.put("error", "job.error.INFERENCE_FAILED");
            return response;
        }
    }

    @GetMapping
    @ResponseBody
    public List<Map<String, Object>> listMixes(@PathVariable String publicId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return List.of();
        return mixService.listForJob(publicId, user.getId()).stream()
                .map(MixController::toJson)
                .toList();
    }

    @GetMapping("/{mixId}/download")
    public void download(@PathVariable String publicId,
                         @PathVariable String mixId,
                         HttpSession session,
                         HttpServletResponse response) throws IOException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendError(401);
            return;
        }

        MixedTrack mix = mixService.getForDownload(mixId, user.getId());
        File file = new File(mix.getFilePath());
        if (!file.exists()) {
            response.sendError(404);
            return;
        }
        String contentType = "wav".equalsIgnoreCase(mix.getFormat()) ? "audio/wav" : "audio/mpeg";
        String safeName = sanitizeFilename(mix.getName()) + "." + mix.getFormat();
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + safeName + "\"");
        response.setContentLengthLong(file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = fis.read(buffer)) != -1) os.write(buffer, 0, n);
        }
    }

    @PostMapping("/{mixId}/delete")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable String publicId,
                                      @PathVariable String mixId,
                                      HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("error", "UNAUTHORIZED");
            return response;
        }
        try {
            mixService.deleteMix(mixId, user.getId());
            response.put("ok", true);
        } catch (IOException e) {
            logger.error("Mix delete IOException: {}", e.getMessage(), e);
            response.put("error", "storage.delete.error");
        }
        return response;
    }

    private static Map<String, Object> toJson(MixedTrack m) {
        Map<String, Object> j = new LinkedHashMap<>();
        j.put("mixId", m.getPublicId());
        j.put("name", m.getName());
        j.put("stemTypes", m.getStemTypes());
        j.put("format", m.getFormat());
        j.put("fileSize", m.getFileSize());
        j.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
        j.put("downloadUrl", "/job/" + m.getJob().getPublicId() + "/mix/" + m.getPublicId() + "/download");
        return j;
    }

    private static String sanitizeFilename(String name) {
        return name.replaceAll("[^A-Za-z0-9._+\\- ]", "_");
    }
}
