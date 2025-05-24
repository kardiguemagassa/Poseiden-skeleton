package com.nnk.springboot.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom successful authentication handler.
 * <p>
 *     This class is triggered after successful authentication via Spring Security.
 *     It redirects users to a specific page based on their roles.
 * </p>
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Method called automatically after successful authentication.
     * Redirects the user based on their role: currently all users are redirected
     * to "/user/list" whether their role is USER or ADMIN (the frontend handles the rest).
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response
     * @param authentication the authentication object containing the logged in user's information
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String redirectUrl = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_USER"))
                .map(role -> "/user/list") // pour les deux rôles pour l’instant thymeleaf gere le reste
                .findFirst()
                .orElse("/");

        response.sendRedirect(redirectUrl);
    }
}
