package com.nnk.springboot.service.serviceImpl;

import com.nnk.springboot.domain.BidList;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.BidListRepository;
import com.nnk.springboot.service.BidListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BidListServiceImpl implements BidListService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BidListServiceImpl.class);

    BidListRepository bidListRepository;

    public BidListServiceImpl(BidListRepository bidListRepository) {
        this.bidListRepository = bidListRepository;
    }

    @Override
    public List<BidList> findAll() {

        try {
            List<BidList> bidLists = bidListRepository.findAll();

            if (bidLists.isEmpty()) {
                LOGGER.warn("No BidList found");
            } else {
                LOGGER.info("Found {} BidList(s)", bidLists.size());
            }
            return bidLists;
        } catch (Exception e) {
            LOGGER.error("Error retrieving BidList(s) from the database", e);
            throw new CustomDataAccessException("Error retrieving BidList(s) from the database", e);
        }
    }

    @Override
    public BidList findById(Integer id) {
        return bidListRepository.findById(id).orElseThrow(() -> new NotFoundException("BidList", id));
    }

    @Override
    public void save(BidList bidList) {
        checkDuplicateAccount(bidList);
        bidListRepository.save(bidList);
        LOGGER.info("Bid saved successfully: {}", bidList);
    }

    @Override
    public void update(BidList bidList) {

        BidList existingBid = bidListRepository.findById(bidList.getBidListId()).orElseThrow(()
                -> new NotFoundException("BidList", bidList.getBidListId()));

        checkDuplicateAccount(bidList);
        updateBidFields(existingBid, bidList);

        bidListRepository.save(existingBid);
        LOGGER.info("Bid updated successfully: {}", bidList);
    }

    @Override
    public void deleteById(Integer id) {

        if (!bidListRepository.existsById(id)) {
            LOGGER.error("BidList with id {} not found", id);
            throw new NotFoundException("BidList", id);
        }
        bidListRepository.deleteById(id);
        LOGGER.info("BidList with id {} deleted successfully", id);

    }

    private void checkDuplicateAccount (BidList bidList) {

        Optional<BidList> duplicate = bidListRepository.findByAccount(bidList.getAccount());

        if (duplicate.isPresent() && !duplicate.get().getBidListId().equals(bidList.getBidListId())) {
            LOGGER.error("Another BidList already exists for account: {}", bidList.getAccount());
            throw new AlreadyExistsException(bidList.getAccount());
        }

    }

    private void updateBidFields (BidList target, BidList source) {
        target.setAccount(source.getAccount());
        target.setBidQuantity(source.getBidQuantity());
        target.setType(source.getType());
    }
}
