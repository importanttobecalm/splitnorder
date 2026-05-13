package com.stemsep.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "home";
        }
        // Anonim ziyaretçi: animasyonlu landing HTML (static asset, JSP değil)
        return "forward:/static/landing.html";
    }
}
