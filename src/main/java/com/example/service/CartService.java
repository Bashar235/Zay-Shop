package com.example.service;

import com.example.model.Cart;
import com.example.model.CartItem;
import com.example.model.Product;
import com.example.repository.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    return cartRepository.save(cart);
                });
    }

    public void addToCart(String userId, String productId, int quantity) {
        Cart cart = getCart(userId);
        Product product = productService.findById(productId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(new CartItem());
        item.setProductId(productId);
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setQuantity(item.getQuantity() + quantity);
        item.setImageId(product.getImageId()); // Set imageId from Product
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        cart.getItems().add(item);
        cart.setTotalPrice(cart.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum());
        cartRepository.save(cart);
    }

    public void updateCart(String userId, String productId, int quantity) {
        Cart cart = getCart(userId);
        Product product = productService.findById(productId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(new CartItem());

        item.setProductId(productId);
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setImageId(product.getImageId()); // Set imageId from Product

        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        if (quantity > 0) {
            cart.getItems().add(item);
        }
        cart.setTotalPrice(cart.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum());
        cartRepository.save(cart);
    }

    public void removeFromCart(String userId, String productId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        cart.setTotalPrice(cart.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum());
        cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = getCart(userId);
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);
    }
}