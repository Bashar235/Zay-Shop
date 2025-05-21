package com.example.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private String id;
    private String userId;
    private List<OrderItemDTO> items;
    private double totalPrice;
    private LocalDateTime orderDate;
    private String status;
    private String fullName;
    private String address;
    private String city;
    private String zipCode;
    private String country;
    private String paymentMethod;
    private String cardNumber; // For checkout form
    private String expiryDate; // For checkout form
    private String cvv; // For checkout form, not persisted
}