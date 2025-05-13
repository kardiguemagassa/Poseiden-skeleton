package com.nnk.springboot.service.serviceImpl;

import com.nnk.springboot.domain.Trade;
import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeServiceImpl {

    private final Logger LOGGER = LoggerFactory.getLogger(TradeServiceImpl.class);
    private final TradeRepository tradeRepository;

    public TradeServiceImpl(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public List<Trade> findAll() {
        try {
            return tradeRepository.findAll();
        } catch (Exception e) {
            LOGGER.error("Error retrieving Trade(s) from the database", e);
            throw new CustomDataAccessException("Error retrieving Trade(s) from the database", e);
        }
    }

    public Trade findById(Integer id) {
        return tradeRepository.findById(id).orElseThrow(() -> new NotFoundException("Trade not found", id));
    }

    public void save(Trade trade) {
        try {
            tradeRepository.save(trade);
            LOGGER.info("Trade saved successfully {}", trade);
        } catch (Exception e) {
            LOGGER.error("Error saving Trade to the database", e);
            throw new CustomDataAccessException("Error saving Trade to the database", e);
        }
    }

    public void update(Trade trade) {

        if (!tradeRepository.existsById(trade.getTradeId())) {
            throw new NotFoundException("Trade", trade.getTradeId());
        }

        try {
            Trade existingTrade = tradeRepository.findById(trade.getTradeId()).orElseThrow(()
                    -> new NotFoundException("Trade", trade.getTradeId()));

            existingTrade.setAccount(trade.getAccount());
            existingTrade.setType(trade.getType());
            existingTrade.setBuyQuantity(trade.getBuyQuantity());

            tradeRepository.save(existingTrade);
            LOGGER.info("Trade updated successfully {}", trade);
        } catch (Exception e) {
            LOGGER.error("Error updating Trade to the database", e);
            throw new CustomDataAccessException("Error updating Trade to the database", e);
        }
    }

    public void deleteById(Integer id) {

        if (!tradeRepository.existsById(id)) {
            LOGGER.error("Trade with id {} not found", id);
            throw new NotFoundException("Trade", id);
        }

        tradeRepository.deleteById(id);
    }
}
