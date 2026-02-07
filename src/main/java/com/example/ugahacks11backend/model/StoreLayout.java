package com.example.ugahacks11backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "store_layout")
@Data
@NoArgsConstructor
public class StoreLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "aisle", nullable = false)
    private String aisle;

    /** Relative x position on store floor (0-1000 scale) */
    @Column(name = "x", nullable = false)
    private int x;

    /** Relative y position on store floor (0-1000 scale) */
    @Column(name = "y", nullable = false)
    private int y;

    public StoreLayout(UUID productId, String aisle, int x, int y) {
        this.productId = productId;
        this.aisle = aisle;
        this.x = x;
        this.y = y;
    }
}
