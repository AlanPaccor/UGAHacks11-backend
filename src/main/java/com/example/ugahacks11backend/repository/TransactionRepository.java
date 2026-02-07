package com.example.ugahacks11backend.repository;

import com.example.ugahacks11backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Get all transactions for a specific product, newest first
    List<Transaction> findByProductIdOrderByCreatedAtDesc(UUID productId);

    // Get all transactions by barcode
    List<Transaction> findByBarcodeOrderByCreatedAtDesc(String barcode);

    // Get recent transactions across all products (for dashboard)
    List<Transaction> findTop50ByOrderByCreatedAtDesc();
}
