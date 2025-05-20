package com.nnk.springboot.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import com.nnk.springboot.controllers.CurveController;
import com.nnk.springboot.service.serviceImpl.CurvePointServiceImpl;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nnk.springboot.domain.CurvePoint;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import org.mockito.Mockito;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CurveController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CurveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurvePointServiceImpl curvePointService;

    @Test
    void testHome_shouldReturnListView() throws Exception {
        CurvePoint cp1 = new CurvePoint();
        CurvePoint cp2 = new CurvePoint();

        given(curvePointService.findAll()).willReturn(Arrays.asList(cp1, cp2));

        mockMvc.perform(get("/curvePoint/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/list"))
                .andExpect(model().attributeExists("curvePoints"));
    }

    @Test
    void testAddCurvePointForm_shouldReturnAddView() throws Exception {
        mockMvc.perform(get("/curvePoint/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/add"))
                .andExpect(model().attributeExists("curvePoint"));
    }

    @Test
    void testValidate_shouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/curvePoint/validate")
                        .param("curveId", "6")
                        .param("term", "10.0")
                        .param("value", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"))
                .andExpect(flash().attributeExists("success"));

        Mockito.verify(curvePointService).save(any(CurvePoint.class));
    }

    @Test
    void testValidate_shouldReturnFormOnValidationError() throws Exception {
        mockMvc.perform(post("/curvePoint/validate")
                        .param("curveId", "") // champ vide donc invalide
                        .param("term", "")
                        .param("value", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/add"));
    }

    @Test
    void testValidate_shouldReturnFormOnAlreadyExistsException() throws Exception {
        doThrow(new AlreadyExistsException("Already exists"))
                .when(curvePointService).save(any(CurvePoint.class));

        mockMvc.perform(post("/curvePoint/validate")
                        .param("curveId", "3")
                        .param("term", "10.2")
                        .param("value", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/add"))
                .andExpect(model().attributeHasFieldErrors("curvePoint", "value"));
    }

    @Test
    void testShowUpdateForm_shouldReturnUpdateView() throws Exception {
        CurvePoint cp = new CurvePoint();
        cp.setId(1);
        given(curvePointService.findById(1)).willReturn(cp);

        mockMvc.perform(get("/curvePoint/update/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/update"))
                .andExpect(model().attributeExists("curvePoint"));
    }

    @Test
    void testShowUpdateForm_shouldRedirectOnNotFoundException() throws Exception {
        given(curvePointService.findById(1)).willThrow(new NotFoundException("Not found", 1));

        mockMvc.perform(get("/curvePoint/update/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testUpdate_shouldReturnFormOnValidationError() throws Exception {
        mockMvc.perform(post("/curvePoint/update/1")
                        .param("curveId", "") // champ vide donc invalide
                        .param("term", "")
                        .param("value", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/update"));
    }

    @Test
    void testUpdateCurvePoint_shouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/curvePoint/update/1")
                        .param("curveId", "5")
                        .param("term", "20.8")
                        .param("value", "200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"))
                .andExpect(flash().attributeExists("success"));

        Mockito.verify(curvePointService).update(any(CurvePoint.class));
    }

    @Test
    void testUpdateCurvePoint_shouldReturnFormOnAlreadyExistsException() throws Exception {
        doThrow(new AlreadyExistsException("Already exists"))
                .when(curvePointService).update(any(CurvePoint.class));

        mockMvc.perform(post("/curvePoint/update/1")
                        .param( "curveId", "5")
                        .param("term", "50.9")
                        .param("value", "200"))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/update"))
                .andExpect(model().attributeHasFieldErrors("curvePoint", "value"));
    }

    @Test
    void testUpdateCurvePoint_shouldRedirectOnNotFoundException() throws Exception {
        doThrow(new NotFoundException("Not found", 1))
                .when(curvePointService).update(any(CurvePoint.class));

        mockMvc.perform(post("/curvePoint/update/1")
                        .param("curveId", "5")
                        .param("term", "20")
                        .param("value", "200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testDeleteCurvePoint_shouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(get("/curvePoint/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"))
                .andExpect(flash().attributeExists("success"));

        Mockito.verify(curvePointService).deleteById(1);
    }

    @Test
    void testDeleteCurvePoint_shouldRedirectOnNotFoundException() throws Exception {
        doThrow(new NotFoundException("Not found", 1))
                .when(curvePointService).deleteById(1);

        mockMvc.perform(get("/curvePoint/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"))
                .andExpect(flash().attributeExists("error"));
    }
}
