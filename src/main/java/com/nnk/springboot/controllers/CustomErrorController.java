package com.nnk.springboot.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    // ErrorController : Gère toutes les erreurs via le endpoint /error
    // ThymeleafExceptionHandler : Intercepte les exceptions spécifiques et prépare les données
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // Log l'erreur
            logger.error("Erreur {} pour l'URL : {}", statusCode,
                    request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));

            model.addAttribute("errorCode", statusCode);
            model.addAttribute("errorMessage", HttpStatus.valueOf(statusCode).getReasonPhrase());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            }
        }
        return "error/500";
    }

}
