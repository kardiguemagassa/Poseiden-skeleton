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

/**
 * MVC controller for user management.
 * <p>
 *     provides the endpoints needed for user administration:
 *     creation, update, deletion, and consultation.
 * </p>
 *
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * Constructor with dependency injection.
     * @param userService user service
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the list of users.
     * @param model view model
     * @return HTML view of the list
     */
    @GetMapping("/list")
    public String home(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user/list";
    }

    /**
     * Displays the form for adding a user.
     * @param users new empty user
     * @param model view model
     *@return HTML view of the add form
     */
    @GetMapping("/add")
    public String addUser(Users users, Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", users);
        }
        return "user/add";
    }

    /**
     * Validates and saves a new user.
     *
     * @param user user to create
     * @param result validation result
     *@param ra redirect attributes for flash messages
     * @return redirect or error view
     */
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

    /**
     * Displays the update form for a user.
     *
     * @param id user ID to modify
     * @param model view model
     *@return update view
     */
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        Users user = userService.findById(id);
        model.addAttribute("user", user);
        return "user/update";
    }

    /**
     * Updates a user after validation.
     *
     * @param id user ID
     * @param user modified user object
     * @param result validation result
     * @param ra redirect attributes for flash messages
     * @return redirect or view with error
     */
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

    /**
     * Deletes a user.
     *
     * @param id user ID to delete
     * @param ra redirect attributes
     * @return redirect to the list
     */
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes ra) {
        userService.deleteById(id);
        ra.addFlashAttribute("success", "User deleted successfully");
        return "redirect:/user/list?success=user.deleted";
    }
}
