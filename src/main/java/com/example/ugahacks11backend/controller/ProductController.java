package com.example.ugahacks11backend.controller;

import com.example.ugahacks11backend.model.Product;
import com.example.ugahacks11backend.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Add product (admin / setup)
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    // Scan barcode
    @GetMapping("/barcode/{barcode}")
    public Product getByBarcode(@PathVariable String barcode) {
        return productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // All products (dashboard)
    @GetMapping
    public List<Product> getAll() {
        return productRepository.findAll();
    }
}
