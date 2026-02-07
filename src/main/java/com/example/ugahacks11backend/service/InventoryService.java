package com.example.ugahacks11backend.service;

import com.example.ugahacks11backend.dto.TransactionRequest;
import com.example.ugahacks11backend.model.Product;
import com.example.ugahacks11backend.model.WasteLog;
import com.example.ugahacks11backend.repository.ProductRepository;
import com.example.ugahacks11backend.repository.WasteLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final WasteLogRepository wasteLogRepository;

    public InventoryService(ProductRepository productRepository, WasteLogRepository wasteLogRepository) {
        this.productRepository = productRepository;
        this.wasteLogRepository = wasteLogRepository;
    }

    // ─── CHECKOUT (SALE) ─────────────────────────────────────────────
    // Customer buys an item → decrement FRONT inventory.
    // Returns alerts if restocking is needed.
    @Transactional
    public Map<String, Object> checkout(TransactionRequest request) {
        Product product = productRepository.findByBarcode(request.getBarcode())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getBarcode()));

        if (product.getFrontQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough front stock. Available: " + product.getFrontQuantity());
        }

        product.setFrontQuantity(product.getFrontQuantity() - request.getQuantity());
        productRepository.save(product);

        // Build response with optional alert
        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("message", "Checkout successful. Sold " + request.getQuantity() + " of " + product.getName());

        if (product.getFrontQuantity() <= product.getReorderThreshold()) {
            response.put("alert", "⚠️ RESTOCK NEEDED: Front stock for '"
                    + product.getName() + "' is low (" + product.getFrontQuantity() + " remaining).");
        }

        return response;
    }

    // ─── RESTOCK (BACK → FRONT) ─────────────────────────────────────
    // Move items from backroom to shelf. Atomic: subtract back, add front.
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

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("message", "Restocked " + request.getQuantity()
                + " of '" + product.getName() + "' from Back to Front.");

        if (product.getBackQuantity() <= product.getReorderThreshold()) {
            response.put("alert", "📦 ORDER FROM SUPPLIER: Back stock for '"
                    + product.getName() + "' is low (" + product.getBackQuantity() + " remaining).");
        }

        return response;
    }

    // ─── WASTE (LOSS / EXPIRED / DAMAGED) ────────────────────────────
    // Remove items from either FRONT or BACK, and log the waste event.
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

        // Increment the total waste counter on the product
        product.setWasteQuantity(product.getWasteQuantity() + request.getQuantity());
        productRepository.save(product);

        // Log the waste event for analytics
        WasteLog wasteLog = new WasteLog(
                product.getBarcode(),
                product.getName(),
                request.getQuantity(),
                location
        );
        wasteLogRepository.save(wasteLog);

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("wasteLog", wasteLog);
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
}
