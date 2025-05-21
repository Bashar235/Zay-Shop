package com.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private String imageId; // Assuming this is the ID of an Image document
    private double price;
    private double rating;
    private String description;
    private String category;
    private int reviewCount;
    private int stockQuantity;
    private List<String> bookmarks; // Assuming list of user IDs who bookmarked
    private List<Comment> comments; // Assuming a Comment class
    private List<String> sizes;
    private List<String> colors;
    private String brand; // e.g., "Nike"
    private List<String> specifications; // e.g., ["Cotton", "Machine Washable"]

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public Integer getStockQuantity() { return stockQuantity; } // Added getter
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; } // Added setter
    public List<String> getBookmarks() { return bookmarks; }
    public void setBookmarks(List<String> bookmarks) { this.bookmarks = bookmarks; }
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes; }
    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public List<String> getSpecifications() { return specifications; }
    public void setSpecifications(List<String> specifications) { this.specifications = specifications; }

    // For shop-single.html compatibility
    public String getMainImage() { return imageId; }
}