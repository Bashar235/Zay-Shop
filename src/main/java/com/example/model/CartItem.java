package com.example.model;

import lombok.Data;

@Data
public class CartItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private byte[] image;
    private String imageId; // Added to store image identifier
}