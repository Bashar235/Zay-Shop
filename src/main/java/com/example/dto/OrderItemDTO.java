package com.example.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
}