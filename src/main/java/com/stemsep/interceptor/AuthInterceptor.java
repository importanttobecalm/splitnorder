package com.stemsep.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String ctx = request.getContextPath();

        if (path.startsWith(ctx + "/static/")
                || path.startsWith(ctx + "/auth/")
                || path.startsWith(ctx + "/api/auth/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return true;
        }

        response.sendRedirect(ctx + "/auth/login");
        return false;
    }
}
