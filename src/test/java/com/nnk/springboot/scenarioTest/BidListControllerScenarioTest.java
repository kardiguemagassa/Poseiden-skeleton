package com.nnk.springboot.scenarioTest;

import com.nnk.springboot.domain.BidList;
import com.nnk.springboot.repositories.BidListRepository;
import com.nnk.springboot.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(SecurityConfig.class)
public class BidListControllerScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BidListRepository bidListRepository;

    @BeforeEach
    void setup() {
        bidListRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void fullScenarioTest_bidListCRUD() throws Exception {

        mockMvc.perform(post("/bidList/validate")
                        .param("account", "TestAccount")
                        .param("type", "TestType")
                        .param("bidQuantity", "123.45"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"));

        BidList bid = bidListRepository.findAll().getFirst();
        assertThat(bid.getAccount()).isEqualTo("TestAccount");


        mockMvc.perform(post("/bidList/update/" + bid.getBidListId())
                        .param("account", "UpdatedAccount")
                        .param("type", "UpdatedType")
                        .param("bidQuantity", "555.55"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"));


        BidList updated = bidListRepository.findById(bid.getBidListId()).orElseThrow();
        assertThat(updated.getAccount()).isEqualTo("UpdatedAccount");
        assertThat(updated.getBidQuantity()).isEqualTo(555.55);


        mockMvc.perform(get("/bidList/delete/" + bid.getBidListId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"));

        assertThat(bidListRepository.findById(bid.getBidListId())).isEmpty();
    }
}
