package com.example.ugahacks11backend.controller;

import com.example.ugahacks11backend.service.GeminiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final GeminiService geminiService;

    public AnalyticsController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    // GET /analytics/ai-insights — Get AI-powered inventory analysis
    @GetMapping("/ai-insights")
    public Map<String, Object> getAIInsights() {
        try {
            return geminiService.getAIInsights();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch AI insights");
            error.put("message", e.getMessage());
            return error;
        }
    }
}
