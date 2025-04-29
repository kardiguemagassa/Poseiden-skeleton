package com.nnk.springboot.service.serviceImpl;

import com.nnk.springboot.domain.BidList;
import com.nnk.springboot.repositories.BidListRepository;
import com.nnk.springboot.service.BidListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BidListServiceImpl implements BidListService {

    @Autowired
    BidListRepository bidListRepository;

    @Override
    public List<BidList> findAll() {
        return bidListRepository.findAll();
    }

    @Override
    public BidList findById(Integer id) {
        return bidListRepository.findById(id).get();
    }

    @Override
    public void save(BidList bidList) {
        bidListRepository.save(bidList);
    }

    @Override
    public void update(BidList bidList) {
    }

    @Override
    public void deleteById(Integer id) {
        bidListRepository.deleteById(id);

    }

}
