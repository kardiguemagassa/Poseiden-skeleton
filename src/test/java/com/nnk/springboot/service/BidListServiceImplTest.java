package com.nnk.springboot.service;

import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.service.serviceImpl.BidListServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import com.nnk.springboot.domain.BidList;
import com.nnk.springboot.exceptions.AlreadyExistsException;

import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.BidListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BidListServiceImplTest {

    private final Logger LOGGER = LoggerFactory.getLogger(BidListServiceImplTest.class);

    @InjectMocks
    private BidListServiceImpl bidListService;

    @Mock
    private BidListRepository bidListRepository;

    private BidList bid;

    @BeforeEach
    public void setup() {

        LOGGER.info("Setting up BidListServiceImplTest");

        bid = new BidList();
        bid.setBidListId(1);
        bid.setAccount("TestAccount");
        bid.setType("TestType");
        bid.setBidQuantity(100.0);
    }

    @Test
    public void testFindAll_shouldReturnBidList() {

        // Arrange
        when(bidListRepository.findAll()).thenReturn(List.of(bid));

        // Act
        var result = bidListService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestAccount", result.getFirst().getAccount());
        verify(bidListRepository, times(1)).findAll();
    }

    //1
    @Test
    public void testFindAll_shouldReturnListOfBidList_whenNotEmpty() {

        // Arrange
        List<BidList> bidLists = List.of(new BidList(), new BidList());
        when(bidListRepository.findAll()).thenReturn(bidLists);

        // Act
        List<BidList> result = bidListService.findAll();

        // Assert
        assertEquals(2, result.size());
        verify(bidListRepository, times(1)).findAll();
    }

    //2
    @Test
    public void testFindAll_shouldReturnEmptyList_whenNoBidListFound() {
        // Arrange
        when(bidListRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<BidList> result = bidListService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(bidListRepository, times(1)).findAll();
    }

    //3
    @Test
    public void testFindAll_shouldThrowCustomException_whenRepositoryFails() {
        // Arrange
        when(bidListRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        CustomDataAccessException exception = assertThrows(CustomDataAccessException.class, () -> bidListService.findAll());

        assertEquals("Error retrieving BidList(s) from the database", exception.getMessage());
        verify(bidListRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_shouldReturnBidList() {
        // Arrange
        when(bidListRepository.findById(1)).thenReturn(Optional.of(bid));

        // Act
        var result = bidListService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals("TestAccount", result.getAccount());
        verify(bidListRepository, times(1)).findById(1);
    }

    @Test
    public void testFindById_shouldThrowNotFoundException() {
        // Arrange
        when(bidListRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> bidListService.findById(1));
        verify(bidListRepository, times(1)).findById(1);
    }

    @Test
    public void testSave_shouldSaveBidList() {
        // Arrange
        when(bidListRepository.save(bid)).thenReturn(bid);

        // Act
        bidListService.save(bid);

        // Assert
        verify(bidListRepository, times(1)).save(bid);
    }

    @Test
    void testSave_shouldThrowAlreadyExistsException() {
        // Arrange
        BidList bidList = new BidList();
        bidList.setAccount("existingAccount");
        bidList.setBidListId(1);

        BidList existingBidList = new BidList();
        existingBidList.setAccount("existingAccount");
        existingBidList.setBidListId(2);

        when(bidListRepository.findByAccount("existingAccount")).thenReturn(Optional.of(existingBidList));

        // Act and Assert
        assertThrows(AlreadyExistsException.class, () -> bidListService.save(bidList));
    }

    @Test
    public void testUpdate_shouldUpdateBidList() {
        // Arrange
        BidList bid = new BidList();
        bid.setBidListId(1);
        bid.setAccount("OriginalAccount");
        bid.setType("OriginalType");
        bid.setBidQuantity(100.0);

        BidList updatedBid = new BidList();
        updatedBid.setBidListId(1);
        updatedBid.setAccount("UpdatedAccount");
        updatedBid.setType("UpdatedType");
        updatedBid.setBidQuantity(150.0);

        when(bidListRepository.findById(1)).thenReturn(Optional.of(bid));
        when(bidListRepository.save(bid)).thenReturn(bid); // On sauvegarde l'objet "bid", pas "updatedBid"

        // Act
        bidListService.update(updatedBid);

        // Assert
        verify(bidListRepository, times(1)).save(bid); // On vérifie que c’est bien "bid" qui est sauvegardé
        assertEquals("UpdatedAccount", bid.getAccount());
        assertEquals("UpdatedType", bid.getType());
        assertEquals(150.0, bid.getBidQuantity());
    }

    @Test
    public void testUpdate_shouldThrowNotFoundException() {
        // Arrange
        BidList updatedBid = new BidList();
        updatedBid.setBidListId(1);

        when(bidListRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> bidListService.update(updatedBid));
        verify(bidListRepository, times(1)).findById(1);
    }

    @Test
    public void testDeleteById_shouldDeleteBidList() {
        // Arrange
        when(bidListRepository.existsById(1)).thenReturn(true);

        // Act
        bidListService.deleteById(1);

        // Assert
        verify(bidListRepository, times(1)).deleteById(1);
    }

    @Test
    public void testDeleteById_shouldThrowNotFoundException() {
        // Arrange
        when(bidListRepository.existsById(1)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> bidListService.deleteById(1));
        verify(bidListRepository, times(1)).existsById(1);
    }


}
