package com.nnk.springboot.service.serviceImpl;

import com.nnk.springboot.domain.RuleName;
import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.RuleNameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class RuleNameServiceImpl {

    private final Logger LOGGER = Logger.getLogger(RuleNameServiceImpl.class.getName());

    private final RuleNameRepository ruleNameRepository;

    public RuleNameServiceImpl(RuleNameRepository ruleNameRepository) {
        this.ruleNameRepository = ruleNameRepository;
    }

    public List<RuleName> findAll() {
       try {
           return ruleNameRepository.findAll();
       } catch (Exception e) {
           LOGGER.warning("Error retrieving RuleName(s) from the database");
           throw new CustomDataAccessException("Error retrieving RuleName(s) from the database", e);
       }
    }

    public RuleName findById(Integer id) {
        LOGGER.info("Retrieving RuleName with id: " + id);
        return ruleNameRepository.findById(id).orElseThrow(() -> new NotFoundException("RuleName not found", id));
    }

    public void save(RuleName ruleName) {

        try {
            ruleNameRepository.save(ruleName);
            LOGGER.info("RuleName saved successfully: " + ruleName);
        } catch (Exception e) {
            throw new CustomDataAccessException("Error saving RuleName to the database", e);
        }
    }

    public void update(RuleName ruleName) {

        if (!ruleNameRepository.existsById(ruleName.getId())) {
            throw new NotFoundException("RuleName", ruleName.getId());
        }

        RuleName existingRuleName = ruleNameRepository.findById(ruleName.getId()).orElseThrow(()
                -> new NotFoundException("RuleName", ruleName.getId()));

        existingRuleName.setName(ruleName.getName());
        existingRuleName.setDescription(ruleName.getDescription());
        existingRuleName.setSqlStr(ruleName.getSqlStr());
        existingRuleName.setSqlPart(ruleName.getSqlPart());
        existingRuleName.setTemplate(ruleName.getTemplate());

        ruleNameRepository.save(existingRuleName);
    }

    public void deleteById(Integer id) {

        if (!ruleNameRepository.existsById(id)) {
            throw new NotFoundException("RuleName", id);
        }
        ruleNameRepository.deleteById(id);
        LOGGER.info("RuleName deleted successfully" + id);
    }
}
