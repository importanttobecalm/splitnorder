package com.stemsep.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.stream.Collectors;

public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        logger.info("[REQUEST] {} {} | params={} | ip={}",
                request.getMethod(),
                request.getRequestURI(),
                formatParams(request.getParameterMap()),
                request.getRemoteAddr());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            logger.info("[VIEW] {} → view={} | model={}",
                    request.getRequestURI(),
                    modelAndView.getViewName(),
                    modelAndView.getModel());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Object start = request.getAttribute("startTime");
        long duration = start != null ? System.currentTimeMillis() - (Long) start : -1L;
        logger.info("[RESPONSE] {} {} | status={} | duration={}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);
        if (ex != null) {
            logger.error("[ERROR] {} {} | exception={}",
                    request.getMethod(), request.getRequestURI(), ex.toString());
        }
    }

    private String formatParams(Map<String, String[]> params) {
        if (params.isEmpty()) return "{}";
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + mask(e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private String mask(String key, String[] values) {
        String lower = key.toLowerCase();
        if (lower.contains("password") || lower.contains("token") || lower.contains("secret")) {
            return "***";
        }
        return values.length == 1 ? values[0] : String.join(",", values);
    }
}
