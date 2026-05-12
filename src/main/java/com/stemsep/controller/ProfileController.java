package com.stemsep.controller;

import com.stemsep.exception.InvalidCredentialsException;
import com.stemsep.exception.UserNotFoundException;
import com.stemsep.exception.UsernameExistsException;
import com.stemsep.model.User;
import com.stemsep.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    public String profile() {
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam(name = "username") String username,
                                HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        try {
            User updated = userService.updateUsername(user.getId(), username);
            session.setAttribute("user", updated);
            return "redirect:/profile?tab=account&saved=1";
        } catch (UsernameExistsException e) {
            return "redirect:/profile?tab=account&error=username_taken";
        } catch (IllegalArgumentException e) {
            return "redirect:/profile?tab=account&error=username_invalid";
        } catch (UserNotFoundException e) {
            session.invalidate();
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam(name = "currentPassword") String currentPassword,
                                 @RequestParam(name = "newPassword") String newPassword,
                                 HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        try {
            userService.changePassword(user.getId(), currentPassword, newPassword);
            return "redirect:/profile?tab=security&saved=1";
        } catch (InvalidCredentialsException e) {
            return "redirect:/profile?tab=security&error=invalid_current";
        } catch (IllegalArgumentException e) {
            return "redirect:/profile?tab=security&error=password_short";
        } catch (UserNotFoundException e) {
            session.invalidate();
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/delete")
    public String deleteAccount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        try {
            userService.deleteAccount(user.getId());
        } catch (UserNotFoundException ignored) {
            // Zaten silinmiş — yine de oturum kapanmalı.
        }
        session.invalidate();
        logger.info("Account deleted, session invalidated: userId={}", user.getId());
        return "redirect:/";
    }
}
