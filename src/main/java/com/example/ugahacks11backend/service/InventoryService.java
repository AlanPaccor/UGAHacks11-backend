package com.example.ugahacks11backend.service;

import com.example.ugahacks11backend.dto.TransactionRequest;
import com.example.ugahacks11backend.model.Product;
import com.example.ugahacks11backend.model.Transaction;
import com.example.ugahacks11backend.model.WasteLog;
import com.example.ugahacks11backend.repository.ProductRepository;
import com.example.ugahacks11backend.repository.TransactionRepository;
import com.example.ugahacks11backend.repository.WasteLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final WasteLogRepository wasteLogRepository;
    private final TransactionRepository transactionRepository;

    public InventoryService(ProductRepository productRepository, 
                            WasteLogRepository wasteLogRepository,
                            TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.wasteLogRepository = wasteLogRepository;
        this.transactionRepository = transactionRepository;
    }

    // ─── CHECKOUT (SALE) ─────────────────────────────────────────────
    @Transactional
    public Map<String, Object> checkout(TransactionRequest request) {
        Product product = productRepository.findByBarcode(request.getBarcode())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getBarcode()));

        if (product.getFrontQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough front stock. Available: " + product.getFrontQuantity());
        }

        product.setFrontQuantity(product.getFrontQuantity() - request.getQuantity());
        productRepository.save(product);

        // 📝 Log transaction
        Transaction transaction = new Transaction(
                product.getId(),
                product.getBarcode(),
                product.getName(),
                "CHECKOUT",
                -request.getQuantity(), // Negative = removed from inventory
                "FRONT"
        );
        transactionRepository.save(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("transaction", transaction);
        response.put("message", "Checkout successful. Sold " + request.getQuantity() + " of " + product.getName());

        if (product.getFrontQuantity() <= product.getReorderThreshold()) {
            response.put("alert", "⚠️ RESTOCK NEEDED: Front stock for '"
                    + product.getName() + "' is low (" + product.getFrontQuantity() + " remaining).");
        }

        return response;
    }

    // ─── RESTOCK (BACK → FRONT) ─────────────────────────────────────
    @Transactional
    public Map<String, Object> restock(TransactionRequest request) {
        Product product = productRepository.findByBarcode(request.getBarcode())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getBarcode()));

        if (product.getBackQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough back stock. Available: " + product.getBackQuantity());
        }

        product.setBackQuantity(product.getBackQuantity() - request.getQuantity());
        product.setFrontQuantity(product.getFrontQuantity() + request.getQuantity());
        productRepository.save(product);

        // 📝 Log transaction
        Transaction transaction = new Transaction(
                product.getId(),
                product.getBarcode(),
                product.getName(),
                "RESTOCK",
                request.getQuantity(), // Positive = moved to front
                "BACK_TO_FRONT"
        );
        transactionRepository.save(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("transaction", transaction);
        response.put("message", "Restocked " + request.getQuantity()
                + " of '" + product.getName() + "' from Back to Front.");

        if (product.getBackQuantity() <= product.getReorderThreshold()) {
            response.put("alert", "📦 ORDER FROM SUPPLIER: Back stock for '"
                    + product.getName() + "' is low (" + product.getBackQuantity() + " remaining).");
        }

        return response;
    }

    // ─── WASTE (LOSS / EXPIRED / DAMAGED) ────────────────────────────
    @Transactional
    public Map<String, Object> logWaste(TransactionRequest request) {
        Product product = productRepository.findByBarcode(request.getBarcode())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getBarcode()));

        String location = request.getLocation().toUpperCase();

        switch (location) {
            case "FRONT" -> {
                if (product.getFrontQuantity() < request.getQuantity()) {
                    throw new RuntimeException("Not enough front stock to waste. Available: " + product.getFrontQuantity());
                }
                product.setFrontQuantity(product.getFrontQuantity() - request.getQuantity());
            }
            case "BACK" -> {
                if (product.getBackQuantity() < request.getQuantity()) {
                    throw new RuntimeException("Not enough back stock to waste. Available: " + product.getBackQuantity());
                }
                product.setBackQuantity(product.getBackQuantity() - request.getQuantity());
            }
            default -> throw new RuntimeException("Invalid location: " + location + ". Must be FRONT or BACK.");
        }

        product.setWasteQuantity(product.getWasteQuantity() + request.getQuantity());
        productRepository.save(product);

        // Log the waste event
        WasteLog wasteLog = new WasteLog(
                product.getBarcode(),
                product.getName(),
                request.getQuantity(),
                location
        );
        wasteLogRepository.save(wasteLog);

        // 📝 Log transaction
        Transaction transaction = new Transaction(
                product.getId(),
                product.getBarcode(),
                product.getName(),
                "WASTE",
                -request.getQuantity(), // Negative = lost
                location
        );
        transactionRepository.save(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("wasteLog", wasteLog);
        response.put("transaction", transaction);
        response.put("message", "Logged " + request.getQuantity()
                + " waste for '" + product.getName() + "' from " + location + ".");

        return response;
    }

    // ─── WASTE HISTORY ───────────────────────────────────────────────
    public List<WasteLog> getWasteHistory(String barcode) {
        return wasteLogRepository.findByBarcode(barcode);
    }

    public List<WasteLog> getAllWasteHistory() {
        return wasteLogRepository.findAll();
    }

    // ─── TRANSACTION HISTORY ─────────────────────────────────────────
    public List<Transaction> getTransactionsByProductId(UUID productId) {
        return transactionRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<Transaction> getTransactionsByBarcode(String barcode) {
        return transactionRepository.findByBarcodeOrderByCreatedAtDesc(barcode);
    }

    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop50ByOrderByCreatedAtDesc();
    }

    // ─── RECEIVE SHIPMENT (ADD TO BACK STOCK) ────────────────────────
    // This simulates receiving inventory from a supplier
    @Transactional
    public Map<String, Object> receiveShipment(TransactionRequest request) {
        Product product = productRepository.findByBarcode(request.getBarcode())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getBarcode()));

        product.setBackQuantity(product.getBackQuantity() + request.getQuantity());
        productRepository.save(product);

        // 📝 Log transaction
        Transaction transaction = new Transaction(
                product.getId(),
                product.getBarcode(),
                product.getName(),
                "SHIPMENT_RECEIVED",
                request.getQuantity(), // Positive = added to inventory
                "BACK"
        );
        transactionRepository.save(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("transaction", transaction);
        response.put("message", "Received shipment: Added " + request.getQuantity()
                + " of '" + product.getName() + "' to back stock.");

        return response;
    }
}
