package com.stemsep.controller;

import com.stemsep.model.Job;
import com.stemsep.model.User;
import com.stemsep.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HistoryController {

    @Autowired
    private JobService jobService;

    @GetMapping("/history")
    public String showHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        List<Job> jobs = jobService.getJobsByUser(user.getId());
        model.addAttribute("jobs", jobs);
        return "history";
    }
}
