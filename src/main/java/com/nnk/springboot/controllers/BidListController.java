package com.nnk.springboot.controllers;

import com.nnk.springboot.domain.BidList;
import com.nnk.springboot.service.BidListService;
import org.springframework.beans.factory.annotation.Autowired;
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
    // TODO: Inject Bid service

    @Autowired
    BidListService bidListService;


    @RequestMapping("/list")
    public String home(Model model) {
        // TODO: call service find all bids to show to the view
        model.addAttribute("bidList", bidListService.findAll());
        return "bidList/list";
    }

    @GetMapping("/add")
    public String addBidForm(BidList bidList, Model model) {
        model.addAttribute("bidList", bidList);
        return "bidList/add";
    }

    @PostMapping("/validate")
    public String validate(@Valid BidList bid, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // TODO: check data valid and save to db, after saving return bid list
        if (result.hasErrors()) {
           return "bidList/add";
        }
        bidListService.save(bid);
        redirectAttributes.addFlashAttribute("success", "Bid successfully added");

        return "bidList/add";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        // TODO: get Bid by Id and to model then show to the form
        BidList bidList = bidListService.findById(id);
        IllegalArgumentException exception = new IllegalArgumentException("Invalid bid Id:" + id);
        model.addAttribute("bidList", bidList);
        return "bidList/update";
    }

    @PostMapping("/update/{id}")
    public String updateBid(@PathVariable("id") Integer id, @Valid BidList bidList,
                            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // TODO: check required fields, if valid call service to update Bid and return list Bid

        if (result.hasErrors()) {
            return "bidList/update";
        }
        bidList.setBidListId(id);
        bidListService.save(bidList);
       redirectAttributes.addFlashAttribute("success", "Bid successfully updated");

        return "redirect:/bidList/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteBid(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        // TODO: Find Bid by Id and delete the bid, return to Bid list

        bidListService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Bid successfully deleted");

        return "redirect:/bidList/list";
    }
}
