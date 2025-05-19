package com.nnk.springboot.service;

import com.nnk.springboot.domain.Trade;
import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.TradeRepository;
import com.nnk.springboot.service.serviceImpl.TradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradeServiceImplTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private Trade trade;

    @BeforeEach
    void setUp() {
        trade = new Trade();
        trade.setTradeId(1);
        trade.setAccount("Account 1");
        trade.setType("Type 1");
        trade.setBuyQuantity(100.0);
    }

    @Test
    void testFindAll_success() {
        when(tradeRepository.findAll()).thenReturn(List.of(trade));

        List<Trade> result = tradeService.findAll();

        assertThat(result).containsExactly(trade);
    }

    @Test
    void testFindAll_exception() {
        when(tradeRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> tradeService.findAll())
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error retrieving Trade");
    }

    @Test
    void testFindById_success() {
        when(tradeRepository.findById(1)).thenReturn(Optional.of(trade));

        Trade result = tradeService.findById(1);

        assertThat(result).isEqualTo(trade);
    }

    @Test
    void testFindById_notFound() {
        when(tradeRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.findById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trade not found");
    }

    @Test
    void testSave_success() {
        tradeService.save(trade);

        verify(tradeRepository).save(trade);
    }

    @Test
    void testSave_exception() {
        doThrow(new RuntimeException("DB error")).when(tradeRepository).save(any(Trade.class));

        assertThatThrownBy(() -> tradeService.save(trade))
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error saving Trade");
    }

    @Test
    void testUpdate_success() {
        when(tradeRepository.existsById(1)).thenReturn(true);
        when(tradeRepository.findById(1)).thenReturn(Optional.of(trade));

        trade.setAccount("Updated Account");
        tradeService.update(trade);

        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void testUpdate_notFound_onExistsCheck() {
        when(tradeRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> tradeService.update(trade))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trade");
    }

    @Test
    void testUpdate_notFound_onFind() {
        when(tradeRepository.existsById(1)).thenReturn(true);
        when(tradeRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.update(trade))
                .isInstanceOf(CustomDataAccessException.class)
                .hasCauseInstanceOf(NotFoundException.class)
                .hasMessageContaining("Error updating Trade to the database");
    }

    @Test
    void testUpdate_exception() {
        when(tradeRepository.existsById(1)).thenReturn(true);
        when(tradeRepository.findById(1)).thenReturn(Optional.of(trade));
        doThrow(new RuntimeException("DB error")).when(tradeRepository).save(any(Trade.class));

        assertThatThrownBy(() -> tradeService.update(trade))
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error updating Trade");
    }

    @Test
    void testDeleteById_success() {
        when(tradeRepository.existsById(1)).thenReturn(true);

        tradeService.deleteById(1);

        verify(tradeRepository).deleteById(1);
    }

    @Test
    void testDeleteById_notFound() {
        when(tradeRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> tradeService.deleteById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trade");
    }
}
