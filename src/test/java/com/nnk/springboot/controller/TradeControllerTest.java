package com.nnk.springboot.controller;

import com.nnk.springboot.controllers.TradeController;
import com.nnk.springboot.domain.Trade;
import com.nnk.springboot.service.serviceImpl.TradeServiceImpl;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TradeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeServiceImpl tradeService;


    private Trade createValidTrade() {
        Trade trade = new Trade();
        trade.setAccount("AccountTest");
        trade.setType("TypeTest");
        trade.setBuyQuantity(100.0);
        return trade;
    }

    @Test
    void testHome() throws Exception {
        List<Trade> trades = List.of(createValidTrade());
        when(tradeService.findAll()).thenReturn(trades);

        mockMvc.perform(get("/trade/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("trade/list"))
                .andExpect(model().attributeExists("trades"))
                .andExpect(model().attribute("trades", trades));
    }

    @Test
    void testAddTrade() throws Exception {
        mockMvc.perform(get("/trade/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("trade/add"))
                .andExpect(model().attributeExists("trade"));
    }

    @Test
    void testValidateSuccess() throws Exception {

        Trade trade = createValidTrade();

        mockMvc.perform(post("/trade/validate")
                        .param("account", trade.getAccount())
                        .param("type", trade.getType())
                        .param("buyQuantity", trade.getBuyQuantity().toString())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"))
                .andExpect(flash().attribute("success", "Trade added successfully"));

        verify(tradeService, times(1)).save(any(Trade.class));
    }

    @Test
    void testValidateValidationErrors() throws Exception {

        mockMvc.perform(post("/trade/validate")
                        .param("type", "SomeType")
                        .param("buyQuantity", "50")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("trade/add"))
                .andExpect(model().attributeHasFieldErrors("trade", "account"));

        verify(tradeService, never()).save(any(Trade.class));
    }

    @Test
    void testValidateAlreadyExists() throws Exception {

        doThrow(new AlreadyExistsException("Duplicate account"))
                .when(tradeService).save(any(Trade.class));

        mockMvc.perform(post("/trade/validate")
                        .param("account", "AccountTest")
                        .param("type", "TypeTest")
                        .param("buyQuantity", "100")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("trade/add"))
                .andExpect(model().attributeHasFieldErrors("trade", "account"));

        verify(tradeService, times(1)).save(any(Trade.class));
    }

    @Test
    void testShowUpdateFormSuccess() throws Exception {
        Trade trade = createValidTrade();
        trade.setTradeId(1);

        when(tradeService.findById(1)).thenReturn(trade);

        mockMvc.perform(get("/trade/update/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("trade/update"))
                .andExpect(model().attributeExists("trade"))
                .andExpect(model().attribute("trade", trade));
    }

    @Test
    void testShowUpdateFormNotFound() throws Exception {
        doThrow(new NotFoundException("Trade not found with id ",1)).when(tradeService).findById(1);

        mockMvc.perform(get("/trade/update/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testUpdateTradeSuccess() throws Exception {
        Trade trade = createValidTrade();

        mockMvc.perform(post("/trade/update/{id}", 1)
                        .param("account", trade.getAccount())
                        .param("type", trade.getType())
                        .param("buyQuantity", trade.getBuyQuantity().toString())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"))
                .andExpect(flash().attribute("success", "Trade successfully updated"));

        verify(tradeService, times(1)).update(any(Trade.class));
    }

    @Test
    void testUpdateTradeValidationErrors() throws Exception {
        // Pas de param 'account' -> erreur validation
        mockMvc.perform(post("/trade/update/{id}", 1)
                        .param("type", "SomeType")
                        .param("buyQuantity", "100")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("trade/update"))
                .andExpect(model().attributeHasFieldErrors("trade", "account"));

        verify(tradeService, never()).update(any(Trade.class));
    }

    @Test
    void testUpdateTradeAlreadyExists() throws Exception {
        doThrow(new AlreadyExistsException("Duplicate account")).when(tradeService).update(any(Trade.class));

        mockMvc.perform(post("/trade/update/{id}", 1)
                        .param("account", "AccountTest")
                        .param("type", "TypeTest")
                        .param("buyQuantity", "100")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("trade/update"))
                .andExpect(model().attributeHasFieldErrors("trade", "account"));

        verify(tradeService, times(1)).update(any(Trade.class));
    }

    @Test
    void testUpdateTradeNotFound() throws Exception {
        doThrow(new NotFoundException("Trade not found with id ",1)).when(tradeService).update(any(Trade.class));

        mockMvc.perform(post("/trade/update/{id}", 1)
                        .param("account", "AccountTest")
                        .param("type", "TypeTest")
                        .param("buyQuantity", "100")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"))
                .andExpect(flash().attributeExists("error"));

        verify(tradeService, times(1)).update(any(Trade.class));
    }

    @Test
    void testDeleteTradeSuccess() throws Exception {
        mockMvc.perform(get("/trade/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"))
                .andExpect(flash().attribute("success", "Trade successfully deleted"));

        verify(tradeService, times(1)).deleteById(1);
    }

    @Test
    void testDeleteTradeNotFound() throws Exception {
        doThrow(new NotFoundException("Trade not found with id ",1)).when(tradeService).deleteById(1);

        mockMvc.perform(get("/trade/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"))
                .andExpect(flash().attributeExists("error"));

        verify(tradeService, times(1)).deleteById(1);
    }
}
