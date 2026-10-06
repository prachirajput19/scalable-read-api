package com.example.readapi.controller;

import com.example.readapi.dto.Dtos.Analytics;
import com.example.readapi.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /** Cached: compare the first call (slow) with the second call (fast). */
    @GetMapping
    public Analytics getAnalytics() {
        return analyticsService.getAnalytics();
    }

    /** Uncached baseline for benchmarking. */
    @GetMapping("/uncached")
    public Analytics getAnalyticsUncached() {
        return analyticsService.computeAnalytics();
    }
}
