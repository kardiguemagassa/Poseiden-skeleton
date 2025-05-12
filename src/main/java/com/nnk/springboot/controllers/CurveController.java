package com.nnk.springboot.controllers;

import com.nnk.springboot.domain.CurvePoint;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.service.serviceImpl.CurvePointServiceImpl;
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
@RequestMapping("/curvePoint")
public class CurveController {

    private final CurvePointServiceImpl curvePointService;

    public CurveController(CurvePointServiceImpl curvePointService) {
        this.curvePointService = curvePointService;
    }

    @GetMapping("/list")
    public String home(Model model) {

        model.addAttribute("curvePoints", curvePointService.findAll());
        return "curvePoint/list";
    }

    @GetMapping("/add")
    public String addCurvePointForm(CurvePoint curvePoint, Model model) {
        model.addAttribute("curvePoint", curvePoint);
        return "curvePoint/add";
    }

    @PostMapping("/validate")
    public String validate(@Valid CurvePoint curvePoint, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {return "curvePoint/add";}

        try {
            curvePointService.save(curvePoint);
            redirectAttributes.addFlashAttribute("success", "CurvePoint added successfully");
        } catch (AlreadyExistsException e) {
            result.rejectValue("value", "exists", e.getMessage());
            return "curvePoint/add";
        }
        curvePointService.save(curvePoint);
        return "redirect:/curvePoint/list";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {

        try {
            CurvePoint curvePoint = curvePointService.findById(id);
            model.addAttribute("curvePoint", curvePoint);
            return "curvePoint/update";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
        }
        return "redirect:/curvePoint/list";
    }

    @PostMapping("/update/{id}")
    public String updateCurvePoint(@PathVariable("id") Integer id, @Valid CurvePoint curvePoint, BindingResult result,
                                   RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {return "curvePoint/update";}

        try {
            curvePoint.setId(id);
            curvePointService.update(curvePoint);
            redirectAttributes.addFlashAttribute("success", "CurvePoint successfully updated");
            return "redirect:/curvePoint/list";
        } catch (AlreadyExistsException e) {
            result.rejectValue("value", "exists", e.getMessage() );
            return "curvePoint/update";
        } catch (NotFoundException e) {
           result.rejectValue("error", e.getMessage() + id);
            return "redirect:/curvePoint/list";
        }

    }

    @GetMapping("/delete/{id}")
    public String deleteCurvePoint(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

        try {
            curvePointService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "CurvePoint successfully deleted");
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
        }
        return "redirect:/curvePoint/list";
    }
}
