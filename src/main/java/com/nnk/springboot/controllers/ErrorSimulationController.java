package com.nnk.springboot.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorSimulationController {

    //http://localhost:8080/error/500
    @GetMapping("/500")
    public String throwException() {
        throw new RuntimeException("Exception test manuelle");
    }

    @GetMapping("/integrity")
    public String throwDataIntegrityViolation() {
        throw new DataIntegrityViolationException("UNIQUE constraint violation on email field");
    }
}
