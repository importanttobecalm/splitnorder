package com.stemsep.controller;

import com.stemsep.model.Job;
import com.stemsep.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    @Autowired
    private JobService jobService;

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("model") String model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "upload.error.empty");
                return "redirect:/upload";
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                redirectAttributes.addFlashAttribute("error", "upload.error.tooLarge");
                return "redirect:/upload";
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !isValidAudioFile(filename)) {
                redirectAttributes.addFlashAttribute("error", "upload.error.invalidFormat");
                return "redirect:/upload";
            }

            Job job = jobService.createJob(session.getId(), file, model);
            jobService.processJobAsync(job.getId());

            return "redirect:/job/" + job.getId();

        } catch (Exception e) {
            logger.error("Upload error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "upload.error.generic");
            return "redirect:/upload";
        }
    }

    private boolean isValidAudioFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac");
    }
}
