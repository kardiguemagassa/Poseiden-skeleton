package com.nnk.springboot.controller;


import com.nnk.springboot.controllers.LoginController;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLogin_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/app/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testAccessDenied_shouldReturn403ViewWithErrorAttributes() throws Exception {
        mockMvc.perform(get("/app/access-denied"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("request"))
                .andExpect(model().attribute("error", org.hamcrest.Matchers.hasProperty("title", containsString("Permission not allowed"))))
                .andExpect(model().attribute("error", org.hamcrest.Matchers.hasProperty("status", org.hamcrest.Matchers.equalTo(org.springframework.http.HttpStatus.FORBIDDEN))));
    }
}
