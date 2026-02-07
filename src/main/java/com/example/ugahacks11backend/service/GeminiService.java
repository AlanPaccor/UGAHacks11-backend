package com.example.ugahacks11backend.service;

import com.example.ugahacks11backend.model.Product;
import com.example.ugahacks11backend.model.Transaction;
import com.example.ugahacks11backend.repository.ProductRepository;
import com.example.ugahacks11backend.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiService(TransactionRepository transactionRepository, ProductRepository productRepository) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> getAIInsights() throws Exception {
        // Fetch recent data
        List<Transaction> recentTransactions = transactionRepository.findTop50ByOrderByCreatedAtDesc();
        List<Product> allProducts = productRepository.findAll();

        // Build context for AI
        String context = buildAnalyticsContext(recentTransactions, allProducts);

        // Create the prompt
        String prompt = buildPrompt(context);

        // Call Gemini API
        String aiResponse = callGeminiAPI(prompt);

        // Parse and return structured response
        Map<String, Object> result = new HashMap<>();
        result.put("analysis", aiResponse);
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("transactionCount", recentTransactions.size());
        result.put("productCount", allProducts.size());

        return result;
    }

    private String buildAnalyticsContext(List<Transaction> transactions, List<Product> products) {
        StringBuilder context = new StringBuilder();

        // Summarize transaction data
        context.append("=== TRANSACTION HISTORY ===\n");
        Map<String, Long> transactionTypes = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionType, Collectors.counting()));

        context.append("Total Transactions: ").append(transactions.size()).append("\n");
        transactionTypes.forEach((type, count) -> 
            context.append(type).append(": ").append(count).append("\n")
        );

        // Top products by activity
        context.append("\n=== TOP ACTIVE PRODUCTS ===\n");
        Map<String, Long> productActivity = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getProductName, Collectors.counting()));

        productActivity.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> context.append(entry.getKey()).append(": ")
                        .append(entry.getValue()).append(" transactions\n"));

        // Current inventory status
        context.append("\n=== CURRENT INVENTORY STATUS ===\n");
        for (Product product : products) {
            context.append(product.getName())
                    .append(" - Front: ").append(product.getFrontQuantity())
                    .append(", Back: ").append(product.getBackQuantity())
                    .append(", Waste: ").append(product.getWasteQuantity())
                    .append(", Threshold: ").append(product.getReorderThreshold())
                    .append("\n");
        }

        // Calculate waste percentage
        context.append("\n=== WASTE ANALYSIS ===\n");
        int totalWaste = transactions.stream()
                .filter(t -> "WASTE".equals(t.getTransactionType()))
                .mapToInt(t -> Math.abs(t.getQuantity()))
                .sum();

        int totalCheckouts = transactions.stream()
                .filter(t -> "CHECKOUT".equals(t.getTransactionType()))
                .mapToInt(t -> Math.abs(t.getQuantity()))
                .sum();

        context.append("Total Waste: ").append(totalWaste).append(" units\n");
        context.append("Total Sales: ").append(totalCheckouts).append(" units\n");
        if (totalCheckouts > 0) {
            double wastePercentage = (totalWaste * 100.0) / (totalWaste + totalCheckouts);
            context.append("Waste Percentage: ").append(String.format("%.2f", wastePercentage)).append("%\n");
        }

        return context.toString();
    }

    private String buildPrompt(String context) {
        return """
                You are an AI Logistics Manager for a retail inventory system. 
                Analyze the following inventory and transaction data, then provide:
                
                1. **Key Insights**: What patterns do you see? (2-3 bullet points)
                2. **Urgent Actions**: What needs immediate attention? (1-2 items)
                3. **Optimization Tips**: How can they reduce waste and improve stock flow? (2-3 tips)
                4. **Predictions**: Based on current trends, what should they prepare for?
                
                Keep your response concise, actionable, and professional. Use bullet points.
                
                DATA:
                %s
                
                Respond in plain text format with clear sections.
                """.formatted(context);
    }

    private String callGeminiAPI(String prompt) throws Exception {
        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", prompt)
                ))
        ));

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Send request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API error: " + response.body());
        }

        // Parse response
        JsonNode root = objectMapper.readTree(response.body());
        return root.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
    }
}
