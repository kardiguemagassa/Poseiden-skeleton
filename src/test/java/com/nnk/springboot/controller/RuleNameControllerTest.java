package com.nnk.springboot.controller;

import com.nnk.springboot.controllers.RuleNameController;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import com.nnk.springboot.domain.RuleName;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.service.serviceImpl.RuleNameServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(RuleNameController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RuleNameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RuleNameServiceImpl ruleNameService;

    @Test
    @DisplayName("GET /ruleName/list - success")
    void testHome() throws Exception {
        mockMvc.perform(get("/ruleName/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/list"))
                .andExpect(model().attributeExists("ruleNames"));
    }

    @Test
    @DisplayName("GET /ruleName/add - show add form")
    void testAddRuleForm() throws Exception {
        mockMvc.perform(get("/ruleName/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/add"))
                .andExpect(model().attributeExists("ruleName"));
    }

    @Test
    @DisplayName("POST /ruleName/validate - valid rule")
    void testValidateSuccess() throws Exception {
        mockMvc.perform(post("/ruleName/validate")
                        .param("name", "Rule1")
                        .param("description", "Description")
                        .param("json", "{}")
                        .param("template", "Template")
                        .param("sqlStr", "SQL")
                        .param("sqlPart", "Part"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));
    }

    @Test
    void testValidateAlreadyExistsException() throws Exception {

        doThrow(new AlreadyExistsException("User already exists"))
                .when(ruleNameService).save(any(RuleName.class));

        mockMvc.perform(post("/ruleName/validate")
                        .param("name", "Rule1")
                        .param("description", "Description")
                        .param("json", "{}")
                        .param("template", "Template")
                        .param("sqlStr", "SQL")
                        .param("sqlPart", "Part"))
                .andExpect(status().isOk()) // la vue est renvoyée sans redirection
                .andExpect(view().name("ruleName/add"))
                .andExpect(model().attributeHasFieldErrors("ruleName", "name"));
    }

    @Test
    @DisplayName("POST /ruleName/validate - Validation Error")
    void testValidateRuleName_ValidationError() throws Exception {

        mockMvc.perform(post("/ruleName/validate")
                        .param("name", "") // Champ obligatoire vide
                        .param("description", "desc")
                        .param("json", "json")
                        .param("template", "template")
                        .param("sqlStr", "sqlStr")
                        .param("sqlPart", "sqlPart")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/add"))
                .andExpect(model().attributeHasFieldErrors("ruleName", "name"));
    }

    @Test
    @DisplayName("GET /ruleName/update/{id} - valid id")
    void testShowUpdateForm() throws Exception {
        RuleName rule = new RuleName();
        rule.setId(1);
        when(ruleNameService.findById(1)).thenReturn(rule);

        mockMvc.perform(get("/ruleName/update/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/update"))
                .andExpect(model().attributeExists("ruleName"));
    }

    @Test
    @DisplayName("POST /ruleName/update/{id} - update success")
    void testUpdateRuleNameSuccess() throws Exception {

        doNothing().when(ruleNameService).update(any(RuleName.class));

        mockMvc.perform(post("/ruleName/update/{id}", 1)
                        .param("name", "Valid Name")
                        .param("description", "Valid Desc")
                        .param("json", "Valid JSON")
                        .param("template", "Valid Template")
                        .param("sqlStr", "Valid SQL")
                        .param("sqlPart", "Valid SQL Part")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));
    }

    @Test
    @DisplayName("POST /ruleName/update/{id} - AlreadyExistsException")
    void testUpdateRuleNameAlreadyExistsException() throws Exception {

        doThrow(new AlreadyExistsException("Rule already exists"))
                .when(ruleNameService).update(any(RuleName.class));

        mockMvc.perform(post("/ruleName/update/{id}", 1)
                        .param("name", "name")
                        .param("description", "desc")
                        .param("json", "json")
                        .param("template", "template")
                        .param("sqlStr", "sqlStr")
                        .param("sqlPart", "sqlPart")
                        .with(csrf()))
                .andExpect(status().isOk()) // renvoie la vue "ruleName/update" donc 200 OK
                .andExpect(view().name("ruleName/update"))
                .andExpect(model().attributeHasFieldErrors("ruleName", "name"));
    }

    @Test
    @DisplayName("POST /ruleName/update/{id} - NotFoundException")
    void testUpdateRuleNameNotFoundException() throws Exception {

        doThrow(new NotFoundException("Rule not found",1))
                .when(ruleNameService).update(any(RuleName.class));

        mockMvc.perform(post("/ruleName/update/{id}", 1)
                        .param("name", "name")
                        .param("description", "desc")
                        .param("json", "json")
                        .param("template", "template")
                        .param("sqlStr", "sqlStr")
                        .param("sqlPart", "sqlPart")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection()) // redirection vers la liste
                .andExpect(redirectedUrl("/ruleName/list"))
                .andExpect(flash().attributeExists("error")); // le message d'erreur est ajouté en flash
    }

    @Test
    @DisplayName("POST /ruleName/update/{id} - Validation failed")
    void testUpdateRuleNameValidationError() throws Exception {

        mockMvc.perform(post("/ruleName/update/{id}", 1)

                        .param("name", "") // Champ vide, donc @NotBlank échoue
                        .param("description", "desc")
                        .param("json", "json")
                        .param("template", "template")
                        .param("sqlStr", "sqlStr")
                        .param("sqlPart", "sqlPart")
                        .with(csrf()))
                .andExpect(status().isOk()) // 200 car on retourne la vue du formulaire
                .andExpect(view().name("ruleName/update"))
                .andExpect(model().attributeHasFieldErrors("ruleName", "name"));
    }

    @Test
    @DisplayName("GET /ruleName/update/{id} - NotFoundException")
    void testShowUpdateForm_NotFound() throws Exception {

        when(ruleNameService.findById(9))
                .thenThrow(new NotFoundException(" RuleName", 9));

        mockMvc.perform(get("/ruleName/update/{id}", 9))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/update"))
                .andExpect(flash().attribute("error", " RuleNamenot found identifier: 99"));
    }

    @Test
    @DisplayName("GET /ruleName/delete/{id} - delete success")
    void testDeleteRuleNameSuccess() throws Exception {
        mockMvc.perform(get("/ruleName/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));
    }

    @Test
    @DisplayName("GET /ruleName/delete/{id} - NotFoundException")
    void testDeleteRuleNameNotFound() throws Exception {
        doThrow(new NotFoundException("Rule not found",1)).when(ruleNameService).deleteById(1);

        mockMvc.perform(get("/ruleName/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));
    }
}
