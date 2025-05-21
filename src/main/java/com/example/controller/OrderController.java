package com.example.controller;

import com.example.dto.OrderDTO;
import com.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Place a new order
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDTO> placeOrder(@RequestBody OrderDTO orderDTO, Authentication authentication) {
        OrderDTO createdOrder = orderService.createOrder(orderDTO, authentication.getName());
        return ResponseEntity.ok(createdOrder);
    }

    // Get user's order history
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDTO>> getOrderHistory(Authentication authentication) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(authentication.getName());
        return ResponseEntity.ok(orders);
    }

    // Get order details
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // Get all orders (admin only)
    @GetMapping("/all")
    @PreAuthorize ("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // Update order status (admin only)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable String id, @RequestBody String status) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    // Cancel an order
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> cancelOrder(@PathVariable String id, Authentication authentication) {
        orderService.cancelOrder(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}