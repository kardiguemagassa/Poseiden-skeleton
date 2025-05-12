package com.nnk.springboot.controllers;

import com.nnk.springboot.domain.Users;
import com.nnk.springboot.exceptions.DataPersistException;
import com.nnk.springboot.exceptions.AlreadyExistsException;
import com.nnk.springboot.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public String home(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user/list";
    }

    @GetMapping("/add")
    public String addUser(Users users, Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", users);
        }
        return "user/add";
    }

    @PostMapping("/validate")
    public String createUser(@Valid @ModelAttribute("user") Users user, BindingResult result, RedirectAttributes ra) {

        if (result.hasErrors()) {return "user/add";}

        try {
            userService.save(user);
            ra.addFlashAttribute("success", "User created");
        } catch (AlreadyExistsException e) {
            LOGGER.error("Business error {} occurred: {}", e.getErrorCode(), e.getMessage());
            result.rejectValue("username", "duplicate", e.getMessage());
            return "user/add";

        } catch (DataPersistException e) {
            ra.addFlashAttribute("error", "Technical error");
            return "redirect:/user/add";
        }

        return "redirect:/user/list";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        Users user = userService.findById(id);
        model.addAttribute("user", user);
        return "user/update";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Integer id,
                             @Valid @ModelAttribute("user") Users user,
                             BindingResult result,
                             RedirectAttributes ra) {

        if (result.hasErrors()) {
            return "user/update";
        }

        user.setId(id);
        userService.save(user); // La vérification se fait maintenant dans le service
        ra.addFlashAttribute("success", "User updated successfully");

        return "redirect:/user/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteById(id);
        return "redirect:/user/list?success=user.deleted";
    }

}
