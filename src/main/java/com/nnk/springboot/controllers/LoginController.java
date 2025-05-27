package com.nnk.springboot.controllers;

import com.nnk.springboot.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

/**
 * Controller responsible for managing authentication (login) and authorization errors (access denied).
 */
@Controller
@RequestMapping("/app")
public class LoginController {

    /**
     * Displays the custom login page.
     * <p>This endpoint is used by Spring Security as an entry point for user authentication.</p>
     *
     * @return the name of the login view (eg: {@code login.html})
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }


    // http://localhost:8080/admin/home

    /**
     * Manages the display of an error page when a user does not have the necessary permissions.
     * <<p>Ce endpoint est utilisé comme redirection par Spring Security lors des erreurs 403 (access denied).</p>
     *
     * @param request the HTTP request, used to display the blocked URL
     * @param model the Spring MVC model to pass data to the view
     * @return the authorization error view (eg: {@code error/403.html})
     */
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
