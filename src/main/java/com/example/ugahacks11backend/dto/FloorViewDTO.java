package com.example.ugahacks11backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FloorViewDTO {

    private UUID productId;
    private String productName;
    private String barcode;
    private String aisle;
    private int x;
    private int y;
    
    // Inventory metrics
    private int frontQuantity;
    private int frontThreshold;
    private int backQuantity;
    
    // Backend-calculated insights
    private double stockRatio;
    private boolean restockNeeded;
    private boolean backroomReorderNeeded;
    
    // AI-powered annotations (optional)
    private String aiInsight;
    private boolean highWasteZone;
    private boolean highTrafficZone;
}
