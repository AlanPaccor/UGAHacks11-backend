package com.example.ugahacks11backend.config;

import com.example.ugahacks11backend.model.Product;
import com.example.ugahacks11backend.model.StoreLayout;
import com.example.ugahacks11backend.repository.ProductRepository;
import com.example.ugahacks11backend.repository.StoreLayoutRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LayoutSeeder {

    @Bean
    CommandLineRunner seedLayout(ProductRepository productRepository, StoreLayoutRepository layoutRepository) {
        return args -> {
            // Only seed if layout is empty
            if (layoutRepository.count() > 0) {
                System.out.println("✅ Store layout already exists. Skipping seed.");
                return;
            }

            List<Product> products = productRepository.findAll();
            
            if (products.isEmpty()) {
                System.out.println("⚠️ No products found. Add products first before seeding layout.");
                return;
            }

            System.out.println("🏪 Seeding store layout...");

            // Simple grid layout - you can customize these coordinates
            // Using a 1000x1000 coordinate system (0-1000 for both x and y)
            
            int[][] positions = {
                {100, 450},  // Aisle A1 - left side
                {100, 455},  // Aisle A2
                {100, 460},  // Aisle A3
                {100, 800},  // Aisle A4
                {500, 200},  // Aisle B1 - center
                {500, 400},  // Aisle B2
                {500, 600},  // Aisle B3
                {500, 800},  // Aisle B4
                {900, 200},  // Aisle C1 - right side
                {900, 400},  // Aisle C2
            };

            String[] aisles = {"A1", "A2", "A3", "A4", "B1", "B2", "B3", "B4", "C1", "C2"};

            for (int i = 0; i < Math.min(products.size(), positions.length); i++) {
                Product product = products.get(i);
                int[] pos = positions[i];
                String aisle = aisles[i % aisles.length];

                StoreLayout layout = new StoreLayout(product.getId(), aisle, pos[0], pos[1]);
                layoutRepository.save(layout);
            }

            System.out.println("✅ Store layout seeded successfully!");
        };
    }
}
