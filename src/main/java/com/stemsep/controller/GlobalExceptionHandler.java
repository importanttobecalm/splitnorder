package com.stemsep.controller;

import com.stemsep.exception.InvalidTokenException;
import com.stemsep.exception.ResourceNotFoundException;
import com.stemsep.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ NoHandlerFoundException.class, ResourceNotFoundException.class, UserNotFoundException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, HttpServletRequest request) {
        logger.warn("404 Not Found: {} {} | {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        logger.warn("Geçersiz token: {} {}", request.getMethod(), request.getRequestURI());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInternal(Exception ex, HttpServletRequest request) {
        logger.error("500 Internal Server Error: {} {}", request.getMethod(), request.getRequestURI(), ex);
        return "error/500";
    }
}
