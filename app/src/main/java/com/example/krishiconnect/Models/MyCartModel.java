package com.example.krishiconnect.Models;

public class MyCartModel {
    private String itemId;
    private String productName;
    private int totalQuantity;
    private int totalPrice;
    private String currentDate;
    private String currentTime;
    private String imageUrl;

    public MyCartModel() {
        // Required empty constructor for Firebase
    }

    public MyCartModel(String itemId, String productName, int totalQuantity, int totalPrice, String currentDate, String currentTime, String imageUrl) {
        this.itemId = itemId;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
        this.currentDate = currentDate;
        this.currentTime = currentTime;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
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

    public String getItemId() {
        return itemId;
    }

}

