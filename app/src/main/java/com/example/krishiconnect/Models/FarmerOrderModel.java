package com.example.krishiconnect.Models;

public class FarmerOrderModel {
    private String productName;
    private int quantity;
    private double price;

    // Default constructor required for Firebase
    public FarmerOrderModel() {
    }

    // Constructor to initialize the order data
    public FarmerOrderModel(String productName, int quantity, double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
