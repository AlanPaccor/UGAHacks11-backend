package com.example.ugahacks11backend.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "barcode", unique = true, nullable = false)
    private String barcode;

    @Column(name = "name")
    private String name;

    @Column(name = "front_quantity")
    private int frontQuantity;

    @Column(name = "back_quantity")
    private int backQuantity;

    @Column(name = "waste_quantity")
    private int wasteQuantity;

    @Column(name = "reorder_threshold")
    private int reorderThreshold;

    // Required by JPA
    public Product() {}

    // Getters & Setters
    public UUID getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFrontQuantity() {
        return frontQuantity;
    }

    public void setFrontQuantity(int frontQuantity) {
        this.frontQuantity = frontQuantity;
    }

    public int getBackQuantity() {
        return backQuantity;
    }

    public void setBackQuantity(int backQuantity) {
        this.backQuantity = backQuantity;
    }

    public int getWasteQuantity() {
        return wasteQuantity;
    }

    public void setWasteQuantity(int wasteQuantity) {
        this.wasteQuantity = wasteQuantity;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }
}
