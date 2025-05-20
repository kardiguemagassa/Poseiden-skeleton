package com.nnk.springboot.controller;


import com.nnk.springboot.controllers.UserController;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import com.nnk.springboot.domain.Users;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.DataPersistException;
import com.nnk.springboot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private Users user;

    @BeforeEach
    public void setup() {
        user = new Users();
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("Password1@");
        user.setFullname("Test User");
        user.setRole("USER");
    }

    @Test
    public void testHome_shouldReturnUserListView() throws Exception {
        when(userService.findAll()).thenReturn(Arrays.asList(user));

        mockMvc.perform(get("/user/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/list"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    public void testAddUser_shouldReturnAddView() throws Exception {
        mockMvc.perform(get("/user/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/add"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void testCreateUser_shouldRedirectToListOnSuccess() throws Exception {
        mockMvc.perform(post("/user/validate")
                        .param("username", "testuser")
                        .param("password", "Password1@")
                        .param("fullname", "Test User")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/list"));

        verify(userService).save(any(Users.class));
    }

    @Test
    public void testCreateUser_shouldReturnAddOnValidationError() throws Exception {
        mockMvc.perform(post("/user/validate")
                        .param("username", "") // empty username (invalid)
                        .param("password", "Password1@")
                        .param("fullname", "Test User")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/add"));
    }

    @Test
    public void testCreateUser_shouldHandleAlreadyExistsException() throws Exception {
        doThrow(new AlreadyExistsException("User already exists"))
                .when(userService).save(any(Users.class));

        mockMvc.perform(post("/user/validate")
                        .param("username", "testuser")
                        .param("password", "Password1@")
                        .param("fullname", "Test User")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/add"));
    }

    @Test
    public void testCreateUser_shouldHandleDataPersistException() throws Exception {
        doThrow(new DataPersistException("Technical error", new RuntimeException()))
                .when(userService).save(any(Users.class));

        mockMvc.perform(post("/user/validate")
                        .param("username", "testuser")
                        .param("password", "Password1@")
                        .param("fullname", "Test User")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/add"));
    }

    @Test
    public void testShowUpdateForm_shouldReturnUpdateView() throws Exception {
        when(userService.findById(1)).thenReturn(user);

        mockMvc.perform(get("/user/update/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/update"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void testUpdateUser_shouldRedirectToListOnSuccess() throws Exception {
        mockMvc.perform(post("/user/update/1")
                        .param("username", "updateduser")
                        .param("password", "Password1@")
                        .param("fullname", "Updated User")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/list"));

        verify(userService).save(any(Users.class));
    }

    @Test
    public void testUpdateUser_shouldReturnUpdateViewOnError() throws Exception {
        mockMvc.perform(post("/user/update/1")
                        .param("username", "") // Invalid username
                        .param("password", "Password1@")
                        .param("fullname", "Updated User")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/update"));
    }

    @Test
    public void testDeleteUser_shouldRedirectToList() throws Exception {
        mockMvc.perform(get("/user/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/list?success=user.deleted"));

        verify(userService).deleteById(1);
    }
}
