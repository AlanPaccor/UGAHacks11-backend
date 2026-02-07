package com.example.ugahacks11backend.dto;

import lombok.Data;

@Data
public class TransactionRequest {

    private String barcode;
    private int quantity;

    /**
     * Location where the action applies: "FRONT" or "BACK".
     * Used for waste logging to know which stock to decrement.
     */
    private String location;
}
