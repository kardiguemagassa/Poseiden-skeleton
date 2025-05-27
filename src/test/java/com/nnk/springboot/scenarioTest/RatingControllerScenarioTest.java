package com.nnk.springboot.scenarioTest;

import com.nnk.springboot.repositories.RatingRepository;
import com.nnk.springboot.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.nnk.springboot.domain.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
public class RatingControllerScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingRepository ratingRepository;

    @BeforeEach
    void setup() {
        ratingRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void fullScenarioTest_ratingCRUD() throws Exception {

        mockMvc.perform(post("/rating/validate")
                        .param("moodysRating", "Aaa")
                        .param("sandPRating", "AAA")
                        .param("fitchRating", "AAA")
                        .param("orderNumber", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"));

        Rating rating = ratingRepository.findAll().getFirst();
        assertThat(rating.getMoodysRating()).isEqualTo("Aaa");

        mockMvc.perform(post("/rating/update/" + rating.getId())
                        .param("moodysRating", "Bbb")
                        .param("sandPRating", "BBB")
                        .param("fitchRating", "BBB")
                        .param("orderNumber", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"));

        Rating updated = ratingRepository.findById(rating.getId()).orElseThrow();
        assertThat(updated.getMoodysRating()).isEqualTo("Bbb");
        assertThat(updated.getOrderNumber()).isEqualTo(2);

        mockMvc.perform(get("/rating/delete/" + updated.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"));

        assertThat(ratingRepository.findById(updated.getId())).isEmpty();
    }

}
