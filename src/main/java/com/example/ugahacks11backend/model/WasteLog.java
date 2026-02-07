package com.example.ugahacks11backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "waste_logs")
@Data
@NoArgsConstructor
public class WasteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "barcode", nullable = false)
    private String barcode;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    /** "FRONT" or "BACK" — where the waste came from */
    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public WasteLog(String barcode, String productName, int quantity, String location) {
        this.barcode = barcode;
        this.productName = productName;
        this.quantity = quantity;
        this.location = location;
        this.timestamp = LocalDateTime.now();
    }
}
