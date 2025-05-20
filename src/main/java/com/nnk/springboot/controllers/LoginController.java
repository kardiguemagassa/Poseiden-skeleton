package com.nnk.springboot.controllers;

import com.nnk.springboot.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/app")
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // http://localhost:8080/admin/home
    @GetMapping("/access-denied")
    public String accessDenied(HttpServletRequest request, Model model) {
        ErrorResponse error = new ErrorResponse();
        error.setTitle("Permission not allowed");
        error.setStatus(HttpStatus.FORBIDDEN);
        error.setMessage("You have not permissions to access this resource.");
        error.setDetails("URL "  + request.getRequestURI ());
        error.setReference("ERR-" + System.currentTimeMillis());
        error.setTimestamp(LocalDateTime.now());

        model.addAttribute("error", error);
        model.addAttribute("request", request);

        return "error/403";
    }
}
