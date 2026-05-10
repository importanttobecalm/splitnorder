package com.stemsep.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // Statik kaynaklar ve API auth endpointleri için geçişe izin ver
        if (path.startsWith(request.getContextPath() + "/static/") || 
            path.startsWith(request.getContextPath() + "/api/auth/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return true;
        }

        // Giriş yapılmamışsa React login sayfasına yönlendir
        response.sendRedirect("http://localhost:5173/login");
        return false;
    }
}
