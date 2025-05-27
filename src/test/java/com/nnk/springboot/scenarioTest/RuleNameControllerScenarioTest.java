package com.nnk.springboot.scenarioTest;

import com.nnk.springboot.security.SecurityConfig;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.nnk.springboot.domain.RuleName;
import com.nnk.springboot.repositories.RuleNameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(SecurityConfig.class)
public class RuleNameControllerScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RuleNameRepository ruleNameRepository;

    @BeforeEach
    void setup() {
        ruleNameRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testFullScenarioForRuleName() throws Exception {

        mockMvc.perform(post("/ruleName/validate")
                        .param("name", "Rule1")
                        .param("description", "desc")
                        .param("json", "json")
                        .param("template", "template")
                        .param("sqlStr", "sql")
                        .param("sqlPart", "part"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));


        mockMvc.perform(get("/ruleName/list"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ruleNames"));

        RuleName rule = ruleNameRepository.findAll().getFirst();
        mockMvc.perform(get("/ruleName/update/" + rule.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/update"));


        mockMvc.perform(post("/ruleName/update/" + rule.getId())
                        .param("name", "UpdatedRule")
                        .param("description", "desc")
                        .param("json", "json")
                        .param("template", "template")
                        .param("sqlStr", "sql")
                        .param("sqlPart", "part"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));

        mockMvc.perform(get("/ruleName/delete/" + rule.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));
    }

}
