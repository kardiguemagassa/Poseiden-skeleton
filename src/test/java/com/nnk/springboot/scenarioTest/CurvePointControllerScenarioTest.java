package com.nnk.springboot.scenarioTest;

import com.nnk.springboot.repositories.CurvePointRepository;
import com.nnk.springboot.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.nnk.springboot.domain.CurvePoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(SecurityConfig.class)
public class CurvePointControllerScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurvePointRepository curvePointRepository;

    @BeforeEach
    void setup() {
        curvePointRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void fullScenarioTest_curvePointCRUD() throws Exception {

        mockMvc.perform(post("/curvePoint/validate")
                        .param("curveId", "10")
                        .param("term", "1.5")
                        .param("value", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"));


        CurvePoint created = curvePointRepository.findAll().getFirst();
        assertThat(created.getCurveId()).isEqualTo(10);
        assertThat(created.getTerm()).isEqualTo(1.5);
        assertThat(created.getValue()).isEqualTo(100.0);

        mockMvc.perform(post("/curvePoint/update/" + created.getId())
                        .param("curveId", "20")
                        .param("term", "2.0")
                        .param("value", "200.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"));

        CurvePoint updated = curvePointRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getCurveId()).isEqualTo(20);
        assertThat(updated.getTerm()).isEqualTo(2.0);
        assertThat(updated.getValue()).isEqualTo(200.0);

        mockMvc.perform(get("/curvePoint/delete/" + updated.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"));

        assertThat(curvePointRepository.findById(updated.getId())).isEmpty();
    }
}
