package com.example.ugahacks11backend.service;

import com.example.ugahacks11backend.dto.FloorViewDTO;
import com.example.ugahacks11backend.model.Product;
import com.example.ugahacks11backend.model.StoreLayout;
import com.example.ugahacks11backend.model.Transaction;
import com.example.ugahacks11backend.repository.ProductRepository;
import com.example.ugahacks11backend.repository.StoreLayoutRepository;
import com.example.ugahacks11backend.repository.TransactionRepository;
import com.example.ugahacks11backend.repository.WasteLogRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FloorViewService {

    private final ProductRepository productRepository;
    private final StoreLayoutRepository storeLayoutRepository;
    private final TransactionRepository transactionRepository;
    private final WasteLogRepository wasteLogRepository;

    public FloorViewService(ProductRepository productRepository,
                            StoreLayoutRepository storeLayoutRepository,
                            TransactionRepository transactionRepository,
                            WasteLogRepository wasteLogRepository) {
        this.productRepository = productRepository;
        this.storeLayoutRepository = storeLayoutRepository;
        this.transactionRepository = transactionRepository;
        this.wasteLogRepository = wasteLogRepository;
    }

    public List<FloorViewDTO> getFloorView() {
        List<Product> allProducts = productRepository.findAll();
        List<StoreLayout> allLayouts = storeLayoutRepository.findAll();
        
        // Build a map for quick layout lookup
        Map<java.util.UUID, StoreLayout> layoutMap = allLayouts.stream()
                .collect(Collectors.toMap(StoreLayout::getProductId, layout -> layout));

        // Get recent transactions for AI insights
        List<Transaction> recentTransactions = transactionRepository.findTop50ByOrderByCreatedAtDesc();
        
        // Calculate waste by product for high-waste zone detection
        Map<String, Long> wasteByProduct = recentTransactions.stream()
                .filter(t -> "WASTE".equals(t.getTransactionType()))
                .collect(Collectors.groupingBy(Transaction::getBarcode, Collectors.counting()));

        // Calculate checkout frequency for high-traffic zone detection
        Map<String, Long> checkoutByProduct = recentTransactions.stream()
                .filter(t => "CHECKOUT".equals(t.getTransactionType()))
                .collect(Collectors.groupingBy(Transaction::getBarcode, Collectors.counting()));

        List<FloorViewDTO> floorView = new ArrayList<>();

        for (Product product : allProducts) {
            StoreLayout layout = layoutMap.get(product.getId());
            
            // Skip products without layout data
            if (layout == null) {
                continue;
            }

            FloorViewDTO dto = new FloorViewDTO();
            
            // Basic product info
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setBarcode(product.getBarcode());
            
            // Location data
            dto.setAisle(layout.getAisle());
            dto.setX(layout.getX());
            dto.setY(layout.getY());
            
            // Inventory metrics
            dto.setFrontQuantity(product.getFrontQuantity());
            dto.setFrontThreshold(product.getReorderThreshold());
            dto.setBackQuantity(product.getBackQuantity());
            
            // Calculate stock ratio (0.0 = empty, 1.0 = at threshold, >1.0 = well-stocked)
            double stockRatio = product.getReorderThreshold() > 0 
                    ? (double) product.getFrontQuantity() / product.getReorderThreshold()
                    : 0.0;
            dto.setStockRatio(stockRatio);
            
            // Flag restock needs
            dto.setRestockNeeded(product.getFrontQuantity() <= product.getReorderThreshold());
            dto.setBackroomReorderNeeded(product.getBackQuantity() <= product.getReorderThreshold());
            
            // AI-powered zone detection
            long wasteCount = wasteByProduct.getOrDefault(product.getBarcode(), 0L);
            long checkoutCount = checkoutByProduct.getOrDefault(product.getBarcode(), 0L);
            
            dto.setHighWasteZone(wasteCount > 2); // More than 2 waste events recently
            dto.setHighTrafficZone(checkoutCount > 5); // More than 5 checkouts recently
            
            // AI insight generation
            dto.setAiInsight(generateAIInsight(product, stockRatio, checkoutCount, wasteCount));
            
            floorView.add(dto);
        }

        return floorView;
    }

    private String generateAIInsight(Product product, double stockRatio, long checkoutCount, long wasteCount) {
        if (stockRatio < 0.3 && checkoutCount > 5) {
            return "Critical: High demand + low stock";
        } else if (stockRatio < 0.5) {
            return "Restock recommended soon";
        } else if (wasteCount > 3) {
            return "High waste detected - review ordering";
        } else if (checkoutCount > 10) {
            return "High velocity item";
        } else if (stockRatio > 2.0) {
            return "Overstocked - reduce next order";
        } else {
            return null; // No special insight
        }
    }
}
