package com.example.krishiconnect.Models;

public class FarmerOrderModel {
    private String productName;
    private String currentDate;
    private String currentTime;
    private String imageUrl;
    private int totalPrice;
    private int totalQuantity;

    // Default constructor required for Firebase
    public FarmerOrderModel() {
    }

    public FarmerOrderModel(String productName, String currentDate, String currentTime, String imageUrl, int totalPrice, int totalQuantity) {
        this.productName = productName;
        this.currentDate = currentDate;
        this.currentTime = currentTime;
        this.imageUrl = imageUrl;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
    }

    // Getters and Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
