package com.nnk.springboot.controllers;

import com.nnk.springboot.domain.BidList;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.service.BidListService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/bidList")
public class BidListController {

    private final BidListService bidListService;

    public BidListController(BidListService bidListService) {
        this.bidListService = bidListService;
    }

    @RequestMapping("/list")
    public String home(Model model) {

        model.addAttribute("bidLists", bidListService.findAll());
        return "bidList/list";
    }

    @GetMapping("/add")
    public String addBidForm(BidList bidList, Model model) {
        model.addAttribute("bidList", bidList);
        return "bidList/add";
    }

    @PostMapping("/validate")
    public String validate(@Valid BidList bid, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {return "bidList/add";}

        try {
            bidListService.save(bid);
            redirectAttributes.addFlashAttribute("success", "Bid successfully added");
        } catch (AlreadyExistsException e) {
            result.rejectValue("account", "exists", e.getMessage() );
            return "bidList/add";
        }

        return "redirect:/bidList/list";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {

        try {
            BidList bidList = bidListService.findById(id);
            model.addAttribute("bidList", bidList);
            return "bidList/update";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
        }

        return "redirect:/bidList/list";
    }

    @PostMapping("/update/{id}")
    public String updateBid(@PathVariable("id") Integer id, @Valid BidList bidList,
                            BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {return "bidList/update";}

        try {
            bidList.setBidListId(id);
            bidListService.update(bidList);
            redirectAttributes.addFlashAttribute("success", "Bid successfully updated");
            return "redirect:/bidList/list";
        } catch (AlreadyExistsException e) {
            result.rejectValue("account", "exists", e.getMessage() );
            return "bidList/update";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
            return "redirect:/bidList/list";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBid(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

        try {
            bidListService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Bid successfully deleted");
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
        }

        return "redirect:/bidList/list";
    }
}
