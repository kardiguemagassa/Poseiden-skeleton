package com.nnk.springboot.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.nnk.springboot.controllers.ErrorSimulationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ErrorSimulationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ErrorSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testThrowRuntimeException_shouldRender500ErrorPage() throws Exception {
        mockMvc.perform(get("/error/500"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/500"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("request"));
    }

    @Test
    void testThrowDataIntegrityViolation_shouldRedirectToErrorPage() throws Exception {
        mockMvc.perform(get("/error/integrity"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"))
                .andExpect(flash().attributeExists("error"));
    }


}
