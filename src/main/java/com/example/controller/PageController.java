package com.example.controller;

import com.example.model.ContactSubmission;
import com.example.model.Image;
import com.example.service.ContactService;
import com.example.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {
    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    @Autowired
    private ImageService imageService;

    @Autowired
    private ContactService contactService;

    @GetMapping("/about")
    public String about(Model model) {
        addImageAttributes(model);
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("contactSubmission", new ContactSubmission());
        addImageAttributes(model);
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@ModelAttribute ContactSubmission contactSubmission, RedirectAttributes redirectAttributes) {
        contactService.saveSubmission(contactSubmission);
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/contact";
    }

    @GetMapping("/faqs")
    public String faqs(Model model) {
        addImageAttributes(model);
        return "faqs";
    }

    // New: Shared method for image attributes
    private void addImageAttributes(Model model) {
        Image logoImage = imageService.findByName("apple-icon.png");
        Image faviconImage = imageService.findByName("favicon.ico");
        Image aboutHero = imageService.findByName("about-hero.svg");
        Image brand1 = imageService.findByName("brand_01.png");
        Image brand2 = imageService.findByName("brand_02.png");
        Image brand3 = imageService.findByName("brand_03.png");
        Image brand4 = imageService.findByName("brand_04.png");

        model.addAttribute("logoImage", logoImage != null ? logoImage : createEmptyImage("apple-icon.png"));
        model.addAttribute("faviconImage", faviconImage != null ? faviconImage : createEmptyImage("favicon.ico"));
        model.addAttribute("aboutHero", aboutHero != null ? aboutHero : createEmptyImage("about-hero.svg"));
        model.addAttribute("brand1", brand1 != null ? brand1 : createEmptyImage("brand_01.png"));
        model.addAttribute("brand2", brand2 != null ? brand2 : createEmptyImage("brand_02.png"));
        model.addAttribute("brand3", brand3 != null ? brand3 : createEmptyImage("brand_03.png"));
        model.addAttribute("brand4", brand4 != null ? brand4 : createEmptyImage("brand_04.png"));

        logger.debug("Added image attributes for page");
    }

    // New: Helper method for empty Image fallback
    private Image createEmptyImage(String name) {
        logger.warn("Image not found for name: {}", name);
        Image image = new Image();
        image.setName(name);
        image.setContentType(name.endsWith(".svg") ? "image/svg+xml" : "image/png");
        image.setData(new byte[0]);
        return image;
    }
}