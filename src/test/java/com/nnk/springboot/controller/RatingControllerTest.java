package com.nnk.springboot.controller;

import com.nnk.springboot.controllers.RatingController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import com.nnk.springboot.domain.Rating;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.service.serviceImpl.RatingServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingServiceImpl ratingService;

    @Test
    @DisplayName("GET /rating/list - Success")
    void testHome() throws Exception {
        when(ratingService.findAll()).thenReturn(Arrays.asList(new Rating(), new Rating()));

        mockMvc.perform(get("/rating/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/list"))
                .andExpect(model().attributeExists("ratings"));

        verify(ratingService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /rating/add - Success")
    void testAddRatingForm() throws Exception {
        mockMvc.perform(get("/rating/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/add"))
                .andExpect(model().attributeExists("rating"));
    }

    @Test
    @DisplayName("POST /rating/validate - Success")
    void testValidateSuccess() throws Exception {
        doNothing().when(ratingService).save(any(Rating.class));

        mockMvc.perform(post("/rating/validate")
                        .param("moodysRating", "Moodys")
                        .param("sandPRating", "SandP")
                        .param("fitchRating", "Fitch")
                        .param("orderNumber", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"))
                .andExpect(flash().attribute("success", "Rating added successfully"));

        verify(ratingService, times(1)).save(any(Rating.class));
    }

    @Test
    @DisplayName("POST /rating/validate - Validation Error")
    void testValidateValidationError() throws Exception {

        mockMvc.perform(post("/rating/validate")
                        .param("sandPRating", "SandP")
                        .param("fitchRating", "Fitch")
                        .param("orderNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/add"));
    }

    @Test
    @DisplayName("POST /rating/validate - AlreadyExistsException")
    void testValidateAlreadyExists() throws Exception {
        doThrow(new AlreadyExistsException("Duplicate rating")).when(ratingService).save(any(Rating.class));

        mockMvc.perform(post("/rating/validate")
                        .param("moodysRating", "Moodys")
                        .param("sandPRating", "SandP")
                        .param("fitchRating", "Fitch")
                        .param("orderNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/add"))
                .andExpect(model().attributeHasFieldErrors("rating", "moodysRating"));
    }

    @Test
    @DisplayName("GET /rating/update/{id} - Success")
    void testShowUpdateFormSuccess() throws Exception {
        Rating rating = new Rating();
        rating.setId(1);
        when(ratingService.findById(1)).thenReturn(rating);

        mockMvc.perform(get("/rating/update/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/update"))
                .andExpect(model().attributeExists("rating"));
    }

    @Test
    @DisplayName("GET /rating/update/{id} - NotFoundException")
    void testShowUpdateFormNotFound() throws Exception {
        when(ratingService.findById(99)).thenThrow(new NotFoundException("Rating", 99));

        mockMvc.perform(get("/rating/update/{id}", 99))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("POST /rating/update/{id} - Success")
    void testUpdateRatingSuccess() throws Exception {
        doNothing().when(ratingService).update(any(Rating.class));

        mockMvc.perform(post("/rating/update/{id}", 1)
                        .param("moodysRating", "Moodys")
                        .param("sandPRating", "SandP")
                        .param("fitchRating", "Fitch")
                        .param("orderNumber", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"))
                .andExpect(flash().attribute("success", "Rating successfully updated"));
    }

    @Test
    @DisplayName("POST /rating/update/{id} - Validation Error")
    void testUpdateRatingValidationError() throws Exception {

        mockMvc.perform(post("/rating/update/{id}", 1)
                        .param("sandPRating", "SandP")
                        .param("fitchRating", "Fitch")
                        .param("orderNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/update"));
    }

    @Test
    @DisplayName("POST /rating/update/{id} - AlreadyExistsException")
    void testUpdateRatingAlreadyExists() throws Exception {
        doThrow(new AlreadyExistsException("Duplicate rating")).when(ratingService).update(any(Rating.class));

        mockMvc.perform(post("/rating/update/{id}", 1)
                        .param("moodysRating", "Moodys")
                        .param("sandPRating", "SandP")
                        .param("fitchRating", "Fitch")
                        .param("orderNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/update"))
                .andExpect(model().attributeHasFieldErrors("rating", "moodysRating"));
    }

    @Test
    @DisplayName("POST /rating/update/{id} - NotFoundException")
    void testUpdateRatingNotFound() throws Exception {
        doThrow(new NotFoundException("Rating", 1)).when(ratingService).update(any(Rating.class));

        mockMvc.perform(post("/rating/update/{id}", 1)
                        .param("moodysRating", "Moodys")
                        .param("sandPRating", "SandP")
                        .param("fitchRating", "Fitch")
                        .param("orderNumber", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("GET /rating/delete/{id} - Success")
    void testDeleteRatingSuccess() throws Exception {
        doNothing().when(ratingService).deleteById(1);

        mockMvc.perform(get("/rating/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"))
                .andExpect(flash().attribute("success", "Rating successfully deleted"));
    }

    @Test
    @DisplayName("GET /rating/delete/{id} - NotFoundException")
    void testDeleteRatingNotFound() throws Exception {
        doThrow(new NotFoundException("Rating", 99)).when(ratingService).deleteById(99);

        mockMvc.perform(get("/rating/delete/{id}", 99))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"))
                .andExpect(flash().attributeExists("error"));
    }
}
