package com.stemsep.controller;

import com.stemsep.model.Job;
import com.stemsep.model.JobStatus;
import com.stemsep.model.User;
import com.stemsep.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private JobService jobService;

    @GetMapping("/")
    public String home(@RequestParam(value = "jobId", required = false) String jobId,
                       HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "forward:/static/landing.html";
        }

        Job job = resolveJob(user, jobId);
        if (job != null) {
            model.addAttribute("jobId", job.getPublicId());
            model.addAttribute("jobStatus", job.getStatus().name());
            model.addAttribute("jobFilename", job.getOriginalFilename());
        } else {
            // Hiç tamamlanmış işi yok → studio boş durumda açılır, modal yükleme
            // istemi gösterir. AppScreen arka planda statik mock olarak kalır.
            model.addAttribute("jobStatus", "EMPTY");
        }
        return "home";
    }

    /**
     * Kullanıcının görmek istediği işi seç:
     *
     * <ol>
     *   <li>URL'de geçerli {@code jobId} varsa ve kullanıcıya aitse onu döndür</li>
     *   <li>Yoksa en yeni iş (createdAt DESC) — eğer PENDING/PROCESSING ise onu
     *       göster (kullanıcı paralel başka cihazdan başlatmış olabilir, durumu
     *       görsün), aksi takdirde son COMPLETED'ı bul; o da yoksa en yeniyi
     *       (FAILED dahil) göster</li>
     *   <li>Hiç iş yoksa {@code null}</li>
     * </ol>
     */
    private Job resolveJob(User user, String jobId) {
        if (jobId != null && !jobId.isBlank()) {
            Job candidate = jobService.getJobByPublicId(jobId);
            if (candidate != null && candidate.getUser() != null
                    && user.getId().equals(candidate.getUser().getId())) {
                return candidate;
            }
        }
        java.util.List<Job> jobs = jobService.getJobsByUser(user.getId());
        if (jobs.isEmpty()) return null;

        // En yeni iş aktif (henüz tamamlanmadı) → onu yansıt
        Job newest = jobs.get(0);
        if (newest.getStatus() == JobStatus.PENDING || newest.getStatus() == JobStatus.PROCESSING) {
            return newest;
        }

        // Son COMPLETED'ı tercih et (kullanıcının görüntülemek istediği gerçek sonuç)
        for (Job j : jobs) {
            if (j.getStatus() == JobStatus.COMPLETED) return j;
        }

        // FAILED bile olsa en yeniyi göster (sebebi öğrensin)
        return newest;
    }
}
