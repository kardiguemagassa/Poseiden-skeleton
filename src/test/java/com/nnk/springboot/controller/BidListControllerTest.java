package com.nnk.springboot.controller;

import com.nnk.springboot.controllers.BidListController;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.service.serviceImpl.BidListServiceImpl;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.nnk.springboot.domain.BidList;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(BidListController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BidListControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    BidListServiceImpl bidListService;

    @Test
    void testHome_shouldReturnListViewWithBidLists() throws Exception {
        BidList bid1 = new BidList("Account1", "Type1", 10d);
        bid1.setBidListId(1);
        BidList bid2 = new BidList("Account2", "Type2", 20d);
        bid2.setBidListId(2);

        given(bidListService.findAll()).willReturn(Arrays.asList(bid1, bid2));

        mockMvc.perform(get("/bidList/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/list"))
                .andExpect(model().attributeExists("bidLists"));
    }

    @Test
    void testAddBidForm_shouldReturnAddView() throws Exception {
        mockMvc.perform(get("/bidList/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/add"))
                .andExpect(model().attributeExists("bidList"));
    }

    //1
    @Test
    void testValidate_shouldRedirectOnSuccess() throws Exception {

        mockMvc.perform(post("/bidList/validate")
                        .param("account", "Account")
                        .param("type", "Type")
                        .param("bidQuantity", "10.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"))
                .andExpect(flash().attribute("success", "Bid successfully added"));

        then(bidListService).should().save(any(BidList.class));
    }

    // 2
    @Test
    void testValidate_shouldReturnFormOnValidationError() throws Exception {
        mockMvc.perform(post("/bidList/validate")
                        .param("account", "") // champ vide donc invalide
                        .param("type", "Type")
                        .param("bidQuantity", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/add"));
    }

    //3
    @Test
    void testValidate_shouldReturnFormOnAlreadyExists() throws Exception {
        doThrow(new AlreadyExistsException("Already exists"))
                .when(bidListService).save(any(BidList.class));

        mockMvc.perform(post("/bidList/validate")
                        .param("account", "Account")
                        .param("type", "Type")
                        .param("bidQuantity", "10.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/add"))
                .andExpect(model().attributeHasFieldErrors("bidList", "account"));
    }

    //1
    @Test
    void testShowUpdateForm_shouldReturnUpdateView() throws Exception {

        BidList bid = new BidList();
        bid.setBidListId(1);
        given(bidListService.findById(1)).willReturn(bid);

        mockMvc.perform(get("/bidList/update/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/update"))
                .andExpect(model().attributeExists("bidList"));
    }

    //2
    @Test
    void testShowUpdateForm_shouldRedirectOnNotFound() throws Exception {
        given(bidListService.findById(1)).willThrow(new NotFoundException("Bid not found: ",1));

        mockMvc.perform(get("/bidList/update/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"))
                .andExpect(flash().attributeExists("error"));
    }

    //3
    @Test
    void testUpdateBid_shouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/bidList/update/1")
                        .param("account", "Account")
                        .param("type", "Type")
                        .param("bidQuantity", "10.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"))
                .andExpect(flash().attribute("success", "Bid successfully updated"));

        ArgumentCaptor<BidList> captor = ArgumentCaptor.forClass(BidList.class);
        verify(bidListService).update(captor.capture());
        BidList captured = captor.getValue();

        assertThat(captured.getBidListId()).isEqualTo(1);
        assertThat(captured.getAccount()).isEqualTo("Account");
        assertThat(captured.getType()).isEqualTo("Type");
        assertThat(captured.getBidQuantity()).isEqualTo(10.0);
    }

    //4
    @Test
    void testUpdateBid_shouldReturnFormOnValidationError() throws Exception {
        mockMvc.perform(post("/bidList/update/1")
                        .param("account", "") // champ vide donc invalide
                        .param("type", "Type")
                        .param("bidQuantity", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/update"));
    }

    //5
    @Test
    void testUpdateBid_shouldRedirectOnNotFoundException() throws Exception {
        doThrow(new NotFoundException("Not found", 1)).when(bidListService).update(any(BidList.class));

        mockMvc.perform(post("/bidList/update/1")
                        .param("account", "Account")
                        .param("type", "Type")
                        .param("bidQuantity", "10.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"))
                .andExpect(flash().attributeExists("error"));
    }

    //6
    @Test
    void testUpdateBid_shouldReturnFormOnAlreadyExistsException() throws Exception {

        doThrow(new AlreadyExistsException("Already exists"))
                .when(bidListService).update(any(BidList.class));

        mockMvc.perform(post("/bidList/update/1")
                        .param("account", "Account")
                        .param("type", "Type")
                        .param("bidQuantity", "10.0"))
                .andExpect(status().isOk())                       // renvoie la page de formulaire (pas de redirection)
                .andExpect(view().name("bidList/update"))         // on reste sur la vue update
                .andExpect(model().attributeHasFieldErrors("bidList", "account")); // erreur liée à "account"
    }

    @Test
    void testDeleteBid_shouldRedirectToListOnSuccess() throws Exception {
        doNothing().when(bidListService).deleteById(1);

        mockMvc.perform(get("/bidList/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void testDeleteBid_shouldRedirectToListOnNotFound() throws Exception {
        doThrow(new NotFoundException("Bid not found: ", 1)).when(bidListService).deleteById(1);

        mockMvc.perform(get("/bidList/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"))
                .andExpect(flash().attributeExists("error"));
    }
}
