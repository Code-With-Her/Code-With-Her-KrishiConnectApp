package com.example.krishiconnect.Models;

public class CustomerUserModel {
    String name, price, imageUrl;

    public CustomerUserModel() {
    }

    public CustomerUserModel(String name, String price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
