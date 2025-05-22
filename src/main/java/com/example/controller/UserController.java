package com.example.controller;

import com.example.dto.UserProfileDTO;
import com.example.model.Image;
import com.example.model.User;
import com.example.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ImageService imageService;

    @Autowired
    public UserController(UserService userService, ProductService productService,
                          CartService cartService, OrderService orderService,
                          ImageService imageService) {
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.imageService = imageService;
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        logger.info("Accessing dashboard for user: {}", username);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", username);
                    return new RuntimeException("User not found: " + username);
                });
        model.addAttribute("user", user);
        model.addAttribute("bookmarkedProducts", productService.findBookmarkedProducts(username));
        model.addAttribute("orders", orderService.getOrdersByUserId(username));
        addImageAttributes(model);
        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String userProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        logger.info("Accessing profile for user: {}", username);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", username);
                    return new RuntimeException("User not found: " + username);
                });
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName(user.getFullName());
        profileDTO.setEmail(user.getEmail());
        model.addAttribute("user", profileDTO);
        model.addAttribute("bookmarkedProducts", productService.findBookmarkedProducts(username));
        model.addAttribute("orders", orderService.getOrdersByUserId(username));
        addImageAttributes(model);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("user") UserProfileDTO profileDTO,
                                BindingResult result, Authentication authentication,
                                Model model, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        logger.debug("Updating profile for user: {}", username);
        if (result.hasErrors()) {
            model.addAttribute("bookmarkedProducts", productService.findBookmarkedProducts(username));
            model.addAttribute("orders", orderService.getOrdersByUserId(username));
            addImageAttributes(model);
            return "user/profile";
        }
        try {
            userService.updateProfile(username, profileDTO);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/user/profile";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("bookmarkedProducts", productService.findBookmarkedProducts(username));
            model.addAttribute("orders", orderService.getOrdersByUserId(username));
            addImageAttributes(model);
            return "user/profile";
        }
    }

    @GetMapping("/main-profile")
    public String mainProfile() {
        return "redirect:/user/profile";
    }

    @PostMapping("/bookmark/remove")
    public String removeBookmark(@RequestParam String productId, Authentication authentication) {
        String username = authentication.getName();
        productService.removeBookmark(productId, username);
        return "redirect:/user/profile";
    }

    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        return userService.findByUsername(username).isEmpty();
    }

    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return userService.findByEmail(email).isEmpty();
    }

    private void addImageAttributes(Model model) {
        Image logoImage = imageService.findByName("apple-icon.png");
        Image faviconImage = imageService.findByName("favicon.ico");
        if (logoImage == null) {
            logger.warn("Logo image not found, using default");
            model.addAttribute("logoImage", createDefaultImage("apple-icon.png", "/static/images/apple-icon.png"));
        } else {
            model.addAttribute("logoImage", logoImage);
        }
        if (faviconImage == null) {
            logger.warn("Favicon image not found, using default");
            model.addAttribute("faviconImage", createDefaultImage("favicon.ico", "/static/images/favicon.ico"));
        } else {
            model.addAttribute("faviconImage", faviconImage);
        }
        logger.debug("Added image attributes for user page");
    }

    private Image createDefaultImage(String name, String defaultPath) {
        Image image = new Image();
        image.setName(name);
        image.setContentType("image/" + (name.endsWith(".png") ? "png" : "x-icon"));
        image.setDefaultPath(defaultPath);
        return image;
    }
}