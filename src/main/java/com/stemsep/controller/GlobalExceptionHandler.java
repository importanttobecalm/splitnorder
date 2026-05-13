package com.stemsep.controller;

import com.stemsep.exception.AppException;
import com.stemsep.exception.InvalidTokenException;
import com.stemsep.exception.ResourceNotFoundException;
import com.stemsep.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Spring DispatcherServlet üzerinden geçen tüm exception'ların merkezi
 * yakalayıcısı. DispatcherServlet dışında çıkan container-level hatalar
 * için web.xml &lt;error-page&gt; → {@link ErrorController} zinciri çalışır.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ============ 404 ============
    @ExceptionHandler({ NoHandlerFoundException.class,
                        ResourceNotFoundException.class,
                        UserNotFoundException.class,
                        InvalidTokenException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, HttpServletRequest req) {
        logger.warn("404 — {} {} | {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return "error/404";
    }

    // ============ 405 ============
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String handleMethodNotAllowed(Exception ex, HttpServletRequest req) {
        logger.warn("405 — {} {} | {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return "error/500";
    }

    // ============ 413 — upload too large ============
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String handleUploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        logger.warn("413 — {} {} | max upload exceeded", req.getMethod(), req.getRequestURI());
        return "error/500";
    }

    // ============ AppException ailesi (ErrorCode.status'ü kullan) ============
    @ExceptionHandler(AppException.class)
    public String handleAppException(AppException ex,
                                     HttpServletRequest req,
                                     HttpServletResponse res) {
        HttpStatus status = ex.getCode().getStatus();
        res.setStatus(status.value());
        if (status.is5xxServerError()) {
            logger.error("{} — {} {} | code={} msg={}",
                    status.value(), req.getMethod(), req.getRequestURI(),
                    ex.getCode(), ex.getMessage(), ex);
        } else {
            logger.warn("{} — {} {} | code={} msg={}",
                    status.value(), req.getMethod(), req.getRequestURI(),
                    ex.getCode(), ex.getMessage());
        }
        return status == HttpStatus.NOT_FOUND ? "error/404" : "error/500";
    }

    // ============ 500 — son nokta ============
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInternal(Exception ex, HttpServletRequest req) {
        logger.error("500 — {} {}", req.getMethod(), req.getRequestURI(), ex);
        return "error/500";
    }
}
