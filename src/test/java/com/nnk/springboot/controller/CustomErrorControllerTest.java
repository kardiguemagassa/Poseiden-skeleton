package com.nnk.springboot.controller;

import com.nnk.springboot.controllers.CustomErrorController;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CustomErrorController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CustomErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHandle404Error() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/not-found"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("errorCode", 404))
                .andExpect(model().attribute("errorMessage", "Not Found"));
    }

    @Test
    void testHandle403Error() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/access-denied"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"))
                .andExpect(model().attribute("errorCode", 403))
                .andExpect(model().attribute("errorMessage", "Forbidden"));
    }

    @Test
    void testHandle500Error() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/server-error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/500"))
                .andExpect(model().attribute("errorCode", 500))
                .andExpect(model().attribute("errorMessage", "Internal Server Error"));
    }
}
