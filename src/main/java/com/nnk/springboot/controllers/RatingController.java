package com.nnk.springboot.controllers;

import com.nnk.springboot.domain.Rating;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.service.serviceImpl.RatingServiceImpl;
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
@RequestMapping("/rating")
public class RatingController {

    private final RatingServiceImpl ratingService;

    public RatingController(RatingServiceImpl ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/list")
    public String home(Model model) {
       model.addAttribute("ratings", ratingService.findAll());
        return "rating/list";
    }

    @GetMapping("/add")
    public String addRatingForm(Rating rating, Model model) {
        model.addAttribute("rating", rating);
        return "rating/add";
    }

    @PostMapping("/validate")
    public String validate(@Valid Rating rating, BindingResult result, Model model, RedirectAttributes redirectAttributes) {

       if (result.hasErrors()) {return "rating/add";}

       try {
           ratingService.save(rating);
           redirectAttributes.addFlashAttribute("success", "Rating added successfully");
           return "redirect:/rating/list";
       } catch (AlreadyExistsException e) {
           result.rejectValue("moodysRating", "exists", e.getMessage());
           return "rating/add";
       }
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {

        try {
            Rating rating = ratingService.findById(id);
            model.addAttribute("rating", rating);
            return "rating/update";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
        }

        return "redirect:/rating/list";
    }

    @PostMapping("/update/{id}")
    public String updateRating(@PathVariable("id") Integer id, @Valid Rating rating, BindingResult result,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {return "rating/update";}

        try {
            rating.setId(id);
            ratingService.update(rating);
            redirectAttributes.addFlashAttribute("success", "Rating successfully updated");
            return "redirect:/rating/list";
        } catch (AlreadyExistsException e) {
            result.rejectValue("moodysRating", "exists", e.getMessage() );
            return "rating/update";
        } catch (NotFoundException e) {
           redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
            return "redirect:/rating/list";
        }

    }

    @GetMapping("/delete/{id}")
    public String deleteRating(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {

        try {
            ratingService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Rating successfully deleted");
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
        }

        return "redirect:/rating/list";
    }
}
