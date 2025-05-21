package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
@Data
public class Order {
    @Id
    private String id;
    private String userId; // References User
    private List<OrderItem> items = new ArrayList<>();
    private double totalPrice;
    private LocalDateTime orderDate;
    private OrderStatus status = OrderStatus.PENDING;
    private String fullName;
    private String address;
    private String city;
    private String zipCode;
    private String country;
    private String paymentMethod; // e.g., "CREDIT_CARD"
    private String cardNumber; // Stores last 4 digits, e.g., "**** **** **** 3456"
    private String expiryDate;

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}