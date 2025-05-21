package com.example.controller;

import com.example.model.Image;
import com.example.model.Product;
import com.example.service.CategoryService;
import com.example.service.ImageService;
import com.example.service.ProductService;
import com.example.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageService imageService;
    private final CartService cartService;

    @Autowired
    public HomeController(ProductService productService, CategoryService categoryService,
                          ImageService imageService, CartService cartService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Product> featuredProducts = productService.findFeaturedProducts();
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("categories", categoryService.findAll());

        // Updated: Use addImageAttributes for consistency and null handling
        addImageAttributes(model);
        return "index";
    }

    // New: Shared method for image attributes
    private void addImageAttributes(Model model) {
        Image logoImage = imageService.findByName("apple-icon.png");
        Image faviconImage = imageService.findByName("favicon.ico");
        Image banner1 = imageService.findByName("banner_img_01.jpg");
        Image banner2 = imageService.findByName("banner_img_02.jpg");
        Image banner3 = imageService.findByName("banner_img_03.jpg");

        // Updated: Log missing images and provide empty Image fallback
        model.addAttribute("logoImage", logoImage != null ? logoImage : createEmptyImage("apple-icon.png"));
        model.addAttribute("faviconImage", faviconImage != null ? faviconImage : createEmptyImage("favicon.ico"));
        model.addAttribute("banner1", banner1 != null ? banner1 : createEmptyImage("banner_img_01.jpg"));
        model.addAttribute("banner2", banner2 != null ? banner2 : createEmptyImage("banner_img_02.jpg"));
        model.addAttribute("banner3", banner3 != null ? banner3 : createEmptyImage("banner_img_03.jpg"));

        // Removed: categoryImages (unused in index.html, categories use category.image)
        logger.debug("Added image attributes for home page");
    }

    // New: Helper method for empty Image fallback
    private Image createEmptyImage(String name) {
        logger.warn("Image not found for name: {}", name);
        Image image = new Image();
        image.setName(name);
        image.setContentType("image/png");
        image.setData(new byte[0]);
        return image;
    }
}