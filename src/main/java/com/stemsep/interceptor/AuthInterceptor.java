package com.stemsep.interceptor;

import com.stemsep.model.User;
import com.stemsep.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * DEV BYPASS: true iken giriş ekranı gösterilmez, otomatik dev kullanıcısı session'a konur.
     * Üretime almadan önce false yapın (veya silin).
     */
    private static final boolean DEV_BYPASS = true;

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String ctx = request.getContextPath();

        if (path.startsWith(ctx + "/static/") || path.startsWith(ctx + "/auth/")) {
            return true;
        }

        HttpSession session = request.getSession(true);
        if (session.getAttribute("user") != null) {
            return true;
        }

        if (DEV_BYPASS) {
            User dev = authService.getOrCreateDevUser();
            session.setAttribute("user", dev);
            return true;
        }

        response.sendRedirect(ctx + "/auth/login");
        return false;
    }
}
