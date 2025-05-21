package com.example.controller;

import com.example.model.Image;
import com.example.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final ImageService imageService;

    @Autowired
    public LoginController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        addImageAttributes(model);
        return "login";
    }

    @GetMapping("/redirect")
    public String redirectAfterLogin(Authentication authentication) {
        if (authentication != null) {
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
                return "redirect:/user/dashboard";
            }
        }
        return "redirect:/";
    }

    private void addImageAttributes(Model model) {
        Image logoImage = imageService.findByName("apple-icon.png");
        Image faviconImage = imageService.findByName("favicon.ico");
        model.addAttribute("logoImage", logoImage != null ? logoImage : new Image());
        model.addAttribute("faviconImage", faviconImage != null ? faviconImage : new Image());
    }
}