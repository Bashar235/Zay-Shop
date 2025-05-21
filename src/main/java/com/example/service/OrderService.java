package com.example.service;

import com.example.dto.OrderDTO;
import com.example.dto.OrderItemDTO;
import com.example.model.Order;
import com.example.model.OrderItem;
import com.example.model.Order.OrderStatus;
import com.example.model.Product;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    public OrderDTO createOrder(OrderDTO orderDTO, String userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalPrice(orderDTO.getTotalPrice());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setFullName(orderDTO.getFullName());
        order.setAddress(orderDTO.getAddress());
        order.setCity(orderDTO.getCity());
        order.setZipCode(orderDTO.getZipCode());
        order.setCountry(orderDTO.getCountry());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        String cardNumber = orderDTO.getCardNumber();
        if (cardNumber != null && cardNumber.length() >= 4) {
            order.setCardNumber("**** **** **** " + cardNumber.substring(cardNumber.length() - 4));
        } else {
            order.setCardNumber("**** **** **** ****");
        }
        order.setExpiryDate(orderDTO.getExpiryDate());
        // Do not store CVV

        // Convert OrderItemDTO to OrderItem and validate stock
        List<OrderItem> items = orderDTO.getItems().stream().map(dto -> {
            OrderItem item = new OrderItem();
            item.setProductId(dto.getProductId());
            item.setProductName(dto.getProductName());
            item.setPrice(dto.getPrice());
            item.setQuantity(dto.getQuantity());
            // Validate stock
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if (product.getStockQuantity() < dto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + dto.getProductName());
            }
            // Update stock
            product.setStockQuantity(product.getStockQuantity() - dto.getQuantity());
            productRepository.save(product);
            return item;
        }).collect(Collectors.toList());

        order.setItems(items);
        Order savedOrder = orderRepository.save(order);

        // Clear cart after order placement
        cartService.clearCart(userId);

        return mapToDTO(savedOrder);
    }

    public List<OrderDTO> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToDTO(order);
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO updateOrderStatus(String id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase())); // Normalize to uppercase
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
        Order updatedOrder = orderRepository.save(order);
        return mapToDTO(updatedOrder);
    }

    public void cancelOrder(String id, String userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Cannot cancel another user's order");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        // Restore stock
        order.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });
        orderRepository.save(order);
    }

    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus().name());
        dto.setFullName(order.getFullName());
        dto.setAddress(order.getAddress());
        dto.setCity(order.getCity());
        dto.setZipCode(order.getZipCode());
        dto.setCountry(order.getCountry());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCardNumber(order.getCardNumber());
        dto.setExpiryDate(order.getExpiryDate());
        List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setProductId(item.getProductId());
            itemDTO.setProductName(item.getProductName());
            itemDTO.setPrice(item.getPrice());
            itemDTO.setQuantity(item.getQuantity());
            return itemDTO;
        }).collect(Collectors.toList());
        dto.setItems(itemDTOs);
        return dto;
    }
}