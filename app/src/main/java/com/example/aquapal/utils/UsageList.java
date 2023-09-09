package com.example.aquapal.utils;

public class UsageList {
    String category;
    float quantity;

    public UsageList(String category, float quantity) {
        this.category = category;
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
}
