package com.nnk.springboot.service;

import com.nnk.springboot.domain.RuleName;
import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.RuleNameRepository;
import com.nnk.springboot.service.serviceImpl.RuleNameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RuleNameServiceImplTest {

    @Mock
    private RuleNameRepository ruleNameRepository;

    @InjectMocks
    private RuleNameServiceImpl ruleNameService;

    private RuleName ruleName;

    @BeforeEach
    void setUp() {
        ruleName = new RuleName();
        ruleName.setId(1);
        ruleName.setName("Rule 1");
        ruleName.setDescription("Test Description");
        ruleName.setSqlStr("SELECT_FROM test");
        ruleName.setSqlPart("WHERE x = y");
        ruleName.setTemplate("template");
    }

    @Test
    void testFindAll_success() {
        when(ruleNameRepository.findAll()).thenReturn(List.of(ruleName));

        List<RuleName> result = ruleNameService.findAll();

        assertThat(result).containsExactly(ruleName);
    }

    @Test
    void testFindAll_exception() {
        when(ruleNameRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> ruleNameService.findAll())
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error retrieving RuleName");
    }

    @Test
    void testFindById_success() {
        when(ruleNameRepository.findById(1)).thenReturn(Optional.of(ruleName));

        RuleName result = ruleNameService.findById(1);

        assertThat(result).isEqualTo(ruleName);
    }

    @Test
    void testFindById_notFound() {
        when(ruleNameRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleNameService.findById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("RuleName");
    }

    @Test
    void testSave_success() {
        ruleNameService.save(ruleName);

        verify(ruleNameRepository).save(ruleName);
    }

    @Test
    void testSave_exception() {
        doThrow(new RuntimeException("DB error")).when(ruleNameRepository).save(any(RuleName.class));

        assertThatThrownBy(() -> ruleNameService.save(ruleName))
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error saving RuleName");
    }

    @Test
    void testUpdate_success() {
        when(ruleNameRepository.existsById(1)).thenReturn(true);
        when(ruleNameRepository.findById(1)).thenReturn(Optional.of(ruleName));

        ruleName.setDescription("Updated");
        ruleNameService.update(ruleName);

        verify(ruleNameRepository).save(any(RuleName.class));
    }

    @Test
    void testUpdate_notFound_onExistsCheck() {
        when(ruleNameRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> ruleNameService.update(ruleName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("RuleName");
    }

    @Test
    void testUpdate_notFound_onFind() {
        when(ruleNameRepository.existsById(1)).thenReturn(true);
        when(ruleNameRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleNameService.update(ruleName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("RuleName");
    }

    @Test
    void testDeleteById_success() {
        when(ruleNameRepository.existsById(1)).thenReturn(true);

        ruleNameService.deleteById(1);

        verify(ruleNameRepository).deleteById(1);
    }

    @Test
    void testDeleteById_notFound() {
        when(ruleNameRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> ruleNameService.deleteById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("RuleName");
    }
}
