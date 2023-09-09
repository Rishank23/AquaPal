package com.example.aquapal.waterDb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "WaterUsage")
public class WaterUsage {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private float quantity;
    private String category;
    private String description;
    private Date date;

    @Ignore
    public WaterUsage(float quantity, String category, String description, Date date){
        this.quantity = quantity;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    public WaterUsage(int id, float quantity, String category, String description, Date date){
        this.id = id;
        this.quantity = quantity;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
