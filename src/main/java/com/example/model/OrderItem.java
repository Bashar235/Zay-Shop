package com.example.model;

import lombok.Data;

@Data
public class OrderItem {
    private String productId; // References Product
    private String productName;
    private double price;
    private int quantity;
}