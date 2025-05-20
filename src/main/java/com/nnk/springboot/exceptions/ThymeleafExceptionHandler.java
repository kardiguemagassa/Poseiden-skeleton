package com.nnk.springboot.exceptions;

import com.nnk.springboot.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@ControllerAdvice
public class ThymeleafExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThymeleafExceptionHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex, Model model, HttpServletRequest request) {

        String errorRef = "ERR-" + System.currentTimeMillis();
        LOGGER.warn("Page not found: {}", ex.getRequestURL());

        ErrorResponse error = new ErrorResponse();
        error.setTitle("Page not found");
        error.setStatus(HttpStatus.NOT_FOUND);
        error.setMessage("Requested resource could not be found.");
        error.setDetails("URL : " + ex.getRequestURL());
        error.setReference(errorRef);
        error.setTimestamp(LocalDateTime.now());

        model.addAttribute("error", error);
        model.addAttribute("request", request);

        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleAllOtherExceptions(Exception ex, HttpServletRequest request, Model model) {

        String errorRef = "ERR-" + System.currentTimeMillis();
        LOGGER.error("Unexpected error [{}] for URL: {}", errorRef, request.getRequestURI(), ex);

        ErrorResponse error = new ErrorResponse();
        error.setTitle("Technical error");
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        error.setMessage("An unexpected error has occurred.");
        error.setDetails("URL : " + request.getRequestURI());
        error.setTimestamp(LocalDateTime.now());
        error.setReference(errorRef);


        // Thymeleaf
        model.addAttribute("error", error);
        model.addAttribute("request", request);

        // Pour la sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }

        return "error/500";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex, RedirectAttributes redirectAttributes) {

        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        LOGGER.error("Data integrity violation: {}", rootCause);

        redirectAttributes.addFlashAttribute("error", "Data integrity error: " + rootCause);

        return "redirect:/error";
    }

}