package com.stemsep.controller;

import com.stemsep.model.Job;
import com.stemsep.model.User;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    @Autowired
    private JobService jobService;

    /**
     * Eski {@code /upload} sayfası kaldırıldı — yükleme artık studio içinde
     * modal olarak yapılıyor (nav'da "Yeni Ayır" butonu). Geriye dönük
     * uyumluluk için bu GET endpoint'i studio'ya {@code ?upload=1} parametresi
     * ile yönlendirir; main.jsx bunu görüp UploadModal'ı otomatik açar.
     */
    @GetMapping("/upload")
    public String showUploadForm() {
        return "redirect:/?upload=1";
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

            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/auth/login"; // AuthInterceptor sayesinde normalde düşmeyecek
            }

            Job job = jobService.createJob(user, file, model);
            jobService.processJobAsync(job.getId());

            return "redirect:/job/" + job.getPublicId();

        } catch (Exception e) {
            logger.error("Upload error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "upload.error.generic");
            return "redirect:/upload";
        }
    }

    /**
     * JSON sürümü — studio ekranındaki upload modal'ı bu endpoint'i çağırır.
     * Form-based POST /upload geriye dönük uyumluluk için olduğu gibi kalır.
     *
     * <p>Ders pattern'i: {@code @PostMapping(".ajax") @ResponseBody}
     * (ders slaytlarındaki {@code /ogrencileriGetir.ajax} kalıbının doğal
     * genişlemesi — multipart upload ders kapsamında değil).</p>
     *
     * @return Başarı: {@code {jobId, status:"PENDING"}}, hata: {@code {error:"<code>"}}
     */
    @PostMapping("/upload.ajax")
    @ResponseBody
    public Map<String, Object> handleUploadAjax(
            @RequestParam("file") MultipartFile file,
            @RequestParam("model") String model,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("error", "UNAUTHORIZED");
                return response;
            }
            if (file.isEmpty()) {
                response.put("error", "upload.error.empty");
                return response;
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("error", "upload.error.tooLarge");
                return response;
            }
            String filename = file.getOriginalFilename();
            if (filename == null || !isValidAudioFile(filename)) {
                response.put("error", "upload.error.invalidFormat");
                return response;
            }

            Job job = jobService.createJob(user, file, model);
            jobService.processJobAsync(job.getId());

            response.put("jobId", job.getPublicId());
            response.put("status", "PENDING");
            return response;
        } catch (Exception e) {
            logger.error("Upload.ajax error: {}", e.getMessage(), e);
            response.put("error", "upload.error.generic");
            return response;
        }
    }

    private boolean isValidAudioFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac");
    }
}
