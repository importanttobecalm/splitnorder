package com.stemsep.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        logger.info("[REQUEST] {} {} | Params: {} | Session: {} | IP: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString() != null ? request.getQueryString() : "-",
                request.getSession().getId().substring(0, 8) + "...",
                request.getRemoteAddr());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            logger.debug("[VIEW] {} → {}", request.getRequestURI(), modelAndView.getViewName());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        logger.info("[RESPONSE] {} {} | Status: {} | Duration: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);

        if (ex != null) {
            logger.error("[ERROR] {} {} | Exception: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex.getMessage());
        }
    }
}
