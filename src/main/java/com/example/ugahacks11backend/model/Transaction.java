package com.example.ugahacks11backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "barcode", nullable = false)
    private String barcode;

    @Column(name = "product_name")
    private String productName;

    /** CHECKOUT, RESTOCK, or WASTE */
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    /** Quantity moved (positive = added, negative = removed) */
    @Column(name = "quantity", nullable = false)
    private int quantity;

    /** For WASTE: "FRONT" or "BACK". For RESTOCK: "BACK_TO_FRONT" */
    @Column(name = "location")
    private String location;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructor for easy creation
    public Transaction(UUID productId, String barcode, String productName, 
                       String transactionType, int quantity, String location) {
        this.productId = productId;
        this.barcode = barcode;
        this.productName = productName;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.location = location;
        this.createdAt = LocalDateTime.now();
    }
}
