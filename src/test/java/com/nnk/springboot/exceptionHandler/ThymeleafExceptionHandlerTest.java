package com.nnk.springboot.exceptionHandler;

import com.nnk.springboot.dto.ErrorResponse;
import com.nnk.springboot.exceptions.ThymeleafExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ThymeleafExceptionHandlerTest {

    private ThymeleafExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ThymeleafExceptionHandler();
    }

    @Test
    void handleNotFound_ShouldReturn404TemplateWithModelAttributes() {
        // Given
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/non-existent", new HttpHeaders());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/non-existent");

        Model model = new ExtendedModelMap();

        // When
        String view = exceptionHandler.handleNotFound(ex, model, request);

        // Then
        assertEquals("error/404", view);
        assertTrue(model.containsAttribute("error"));
        assertTrue(model.containsAttribute("request"));

        ErrorResponse error = (ErrorResponse) model.getAttribute("error");
        assertEquals("Page not found", Objects.requireNonNull(error).getTitle());
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void handleAllOtherExceptions_ShouldReturn500TemplateWithModelAttributes() {
        // Given
        Exception ex = new RuntimeException("Unexpected");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/error");

        Model model = new ExtendedModelMap();

        // Simuler un utilisateur authentifié dans Spring Security
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        String view = exceptionHandler.handleAllOtherExceptions(ex, request, model);

        // Then
        assertEquals("error/500", view);
        assertTrue(model.containsAttribute("error"));
        assertTrue(model.containsAttribute("request"));
        assertEquals("testuser", model.getAttribute("username"));

        ErrorResponse error = (ErrorResponse) model.getAttribute("error");
        assertEquals("Technical error", Objects.requireNonNull(error).getTitle());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatus());
    }

    @Test
    void handleDataIntegrityViolation_ShouldRedirectWithFlashAttribute() {
        // Given
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Duplicate entry", new Throwable("Duplicate username"));

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // When
        String view = exceptionHandler.handleDataIntegrityViolation(ex, redirectAttributes);

        // Then
        assertEquals("redirect:/error", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Duplicate username"));
    }
}

