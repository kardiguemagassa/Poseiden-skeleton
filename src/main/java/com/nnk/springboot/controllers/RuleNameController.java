package com.nnk.springboot.controllers;

import com.nnk.springboot.domain.RuleName;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.service.serviceImpl.RuleNameServiceImpl;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/ruleName")
public class RuleNameController {

    private final RuleNameServiceImpl ruleNameService;

    public RuleNameController(RuleNameServiceImpl ruleNameService) {
        this.ruleNameService = ruleNameService;
    }

    @GetMapping("/list")
    public String home(Model model) {
        model.addAttribute("ruleNames", ruleNameService.findAll());
        return "ruleName/list";
    }

    @GetMapping("/add")
    public String addRuleForm(RuleName ruleName, Model model) {
        model.addAttribute("ruleName", ruleName);
        return "ruleName/add";
    }

    @PostMapping("/validate")
    public String validate(@Valid RuleName ruleName, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {return "ruleName/add";}

        try {
            ruleNameService.save(ruleName);
            redirectAttributes.addFlashAttribute("success", "RuleName added successfully");
        } catch (AlreadyExistsException e) {
            result.rejectValue("name", "exists", e.getMessage() );
            return "ruleName/add";
        }
        return "redirect:/ruleName/list";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {

        try {
            RuleName ruleName = ruleNameService.findById(id);
            model.addAttribute("ruleName", ruleName);
            return "ruleName/update";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
        }
        return "redirect:/ruleName/update";
    }

    @PostMapping("/update/{id}")
    public String updateRuleName(@PathVariable("id") Integer id, @Valid RuleName ruleName,
                                 BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {return "ruleName/update";}

        try {
            ruleName.setId(id);
            ruleNameService.update(ruleName);
            redirectAttributes.addFlashAttribute("success", "RuleName successfully updated");
            return "redirect:/ruleName/list";
        } catch (AlreadyExistsException e) {
            result.rejectValue("name", "exists", e.getMessage() );
            return "ruleName/update";
        } catch (NotFoundException e) {
           redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
            return "redirect:/ruleName/list";
        }


    }

    @GetMapping("/delete/{id}")
    public String deleteRuleName(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {

        try {
            ruleNameService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "RuleName successfully deleted");
        } catch (NotFoundException e) {
            model.addAttribute("error", e.getMessage() + id);
        }
        return "redirect:/ruleName/list";
    }
}
