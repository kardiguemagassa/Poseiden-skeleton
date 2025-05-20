package com.nnk.springboot.controllers;

import com.nnk.springboot.domain.Trade;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.service.serviceImpl.TradeServiceImpl;
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
@RequestMapping("/trade")
public class TradeController {

    private final TradeServiceImpl tradeService;

    public TradeController(TradeServiceImpl tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/list")
    public String home(Model model) {
        model.addAttribute("trades", tradeService.findAll());
        return "trade/list";
    }

    @GetMapping("/add")
    public String addTrade(Trade trade, Model model) {
        model.addAttribute("trade", trade);
        return "trade/add";
    }

    @PostMapping("/validate")
    public String validate(@Valid Trade trade, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {return "trade/add";}

        try {
            tradeService.save(trade);
            redirectAttributes.addFlashAttribute("success", "Trade added successfully");
        } catch (AlreadyExistsException e) {
            result.rejectValue("account", "exists", e.getMessage() );
            return "trade/add";
        }

        return "redirect:/trade/list";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {

        try {
            Trade trade = tradeService.findById(id);
            model.addAttribute("trade", trade);
            return "trade/update";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
        }
        return "redirect:/trade/list";
    }

    @PostMapping("/update/{id}")
    public String updateTrade(@PathVariable("id") Integer id, @Valid Trade trade,
                             BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {return "trade/update";}

        try {
            trade.setTradeId(id);
            tradeService.update(trade);
            redirectAttributes.addFlashAttribute("success", "Trade successfully updated");
            return "redirect:/trade/list";
        } catch (AlreadyExistsException e) {
            result.rejectValue("account", "exists", e.getMessage() );
            return "trade/update";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
            return "redirect:/trade/list";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteTrade(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

        try {
            tradeService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Trade successfully deleted");
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() + id);
        }
        return "redirect:/trade/list";
    }
}
