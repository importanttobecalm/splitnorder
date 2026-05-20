package com.stemsep.controller;

import com.stemsep.model.Job;
import com.stemsep.model.User;
import com.stemsep.service.JobService;
import com.stemsep.service.StorageQuotaService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class HistoryController {

    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

    @Autowired
    private JobService jobService;

    @Autowired
    private StorageQuotaService storageQuotaService;

    @GetMapping("/history")
    public String showHistory(@RequestParam(name = "q", required = false) String q,
                              HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        List<Job> jobs = (q == null || q.trim().isEmpty())
                ? jobService.getJobsByUser(user.getId())
                : jobService.searchJobsByUser(user.getId(), q);
        model.addAttribute("jobs", jobs);

        long usedBytes = storageQuotaService.getUsedBytes(user.getId());
        int percent = storageQuotaService.getUsagePercent(user.getId());
        model.addAttribute("storageUsedBytes", usedBytes);
        model.addAttribute("storageQuotaBytes", StorageQuotaService.QUOTA_BYTES);
        model.addAttribute("storageRemainingBytes",
                storageQuotaService.getRemainingBytes(user.getId()));
        model.addAttribute("storagePercent", percent);
        model.addAttribute("storageWarn", percent >= StorageQuotaService.WARN_PERCENT && percent < 100);
        model.addAttribute("storageFull", percent >= 100);
        return "history";
    }

    @PostMapping("/history/job/{publicId}/delete")
    public String deleteJob(@PathVariable String publicId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        try {
            jobService.deleteJob(publicId, user.getId());
            redirectAttributes.addFlashAttribute("info", "storage.deleted");
        } catch (IOException e) {
            logger.error("Job silme FS hatası: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "storage.delete.error");
        }
        return "redirect:/history";
    }
}
