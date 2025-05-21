package com.example.controller;

import com.example.model.Cart;
import com.example.model.Image;
import com.example.service.CartService;
import com.example.service.ImageService;
import com.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/cart")
    public String viewCart(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login?cartAccess";
        }
        Cart cart = cartService.getCart(authentication.getName());
        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getItems()); // Added to fix null cartItems
        addImageAttributes(model);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String productId,
                            @RequestParam int quantity,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login?cartAccess";
        }
        try {
            if (quantity <= 0) {
                throw new RuntimeException("Quantity must be positive");
            }
            cartService.addToCart(authentication.getName(), productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Product added to cart!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam String productId,
                             @RequestParam int quantity,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login?cartAccess";
        }
        try {
            if (quantity < 0) {
                throw new RuntimeException("Quantity cannot be negative");
            }
            cartService.updateCart(authentication.getName(), productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam String productId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login?cartAccess";
        }
        try {
            cartService.removeFromCart(authentication.getName(), productId);
            redirectAttributes.addFlashAttribute("success", "Product removed from cart!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login?cartAccess";
        }
        try {
            cartService.clearCart(authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Cart cleared!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String viewCheckout(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login?checkoutAccess";
        }
        Cart cart = cartService.getCart(authentication.getName());
        model.addAttribute("cart", cart);
        addImageAttributes(model);
        return "checkout";
    }

    @GetMapping("/order-confirmation")
    public String viewOrderConfirmation(@RequestParam String orderId, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("order", orderService.getOrderById(orderId));
        addImageAttributes(model);
        return "order-confirmation";
    }

    private void addImageAttributes(Model model) {
        Image logoImage = imageService.findByName("apple-icon.png");
        Image faviconImage = imageService.findByName("favicon.ico");
        model.addAttribute("logoImage", logoImage != null ? logoImage : createEmptyImage("apple-icon.png"));
        model.addAttribute("faviconImage", faviconImage != null ? faviconImage : createEmptyImage("favicon.ico"));
        logger.debug("Added image attributes for cart page");
    }

    private Image createEmptyImage(String name) {
        logger.warn("Image not found for name: {}", name);
        Image image = new Image();
        image.setId("dummy-" + name);
        image.setName(name);
        image.setContentType("image/png");
        image.setData(new byte[0]);
        return image;
    }
}