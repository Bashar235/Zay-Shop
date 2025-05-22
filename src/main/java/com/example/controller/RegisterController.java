package com.example.controller;

import com.example.dto.UserRegistrationDTO;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private final UserService userService;

    @Autowired
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        logger.debug("Displaying registration form");
        model.addAttribute("user", new UserRegistrationDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO,
                               BindingResult result,
                               @RequestParam(value = "adminCode", required = false) String adminCode,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        logger.info("Processing registration for username: {}", userDTO.getUsername());
        logger.debug("UserDTO: username={}, email={}, fullName={}, role={}",
                userDTO.getUsername(), userDTO.getEmail(), userDTO.getFullName(), userDTO.getRole());

        if (result.hasErrors()) {
            logger.error("Validation errors in registration form: {}", result.getAllErrors());
            model.addAttribute("error", "Please correct the errors in the form.");
            return "register";
        }

        try {
            userService.registerUser(userDTO, adminCode);
            logger.info("Registration successful for username: {}", userDTO.getUsername());
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (IllegalStateException e) {
            logger.error("Registration failed: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            logger.error("Unexpected error during registration: {}", e.getMessage(), e);
            model.addAttribute("error", "An unexpected error occurred during registration: " + e.getMessage());
            return "register";
        }
    }
}