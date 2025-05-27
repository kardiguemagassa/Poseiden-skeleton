package com.nnk.springboot.scenarioTest;

import com.nnk.springboot.repositories.TradeRepository;
import com.nnk.springboot.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.nnk.springboot.domain.Trade;
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
public class TradeControllerScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradeRepository tradeRepository;

    @BeforeEach
    void cleanDb() {
        tradeRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void fullScenarioTest_tradeCRUD() throws Exception {

        mockMvc.perform(post("/trade/validate")
                        .param("account", "TestAccount")
                        .param("type", "TestType")
                        .param("buyQuantity", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"));

        Trade trade = tradeRepository.findAll().getFirst();
        assertThat(trade.getAccount()).isEqualTo("TestAccount");
        assertThat(trade.getBuyQuantity()).isEqualTo(100.0);

        mockMvc.perform(post("/trade/update/" + trade.getTradeId())
                        .param("account", "UpdatedAccount")
                        .param("type", "UpdatedType")
                        .param("buyQuantity", "250.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"));

        Trade updatedTrade = tradeRepository.findById(trade.getTradeId()).orElseThrow();
        assertThat(updatedTrade.getAccount()).isEqualTo("UpdatedAccount");
        assertThat(updatedTrade.getBuyQuantity()).isEqualTo(250.5);

        mockMvc.perform(get("/trade/delete/" + trade.getTradeId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"));

        assertThat(tradeRepository.findById(trade.getTradeId())).isEmpty();
    }
}
