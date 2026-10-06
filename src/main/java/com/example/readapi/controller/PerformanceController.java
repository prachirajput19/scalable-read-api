package com.example.readapi.controller;

import com.example.readapi.dto.Dtos.NPlusOneComparison;
import com.example.readapi.service.PerformanceService;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final CacheManager cacheManager;

    public PerformanceController(PerformanceService performanceService, CacheManager cacheManager) {
        this.performanceService = performanceService;
        this.cacheManager = cacheManager;
    }

    /** GET /api/perf/n-plus-one - runs both strategies and reports SQL statement counts + time. */
    @GetMapping("/perf/n-plus-one")
    public NPlusOneComparison nPlusOne() {
        return performanceService.compareNPlusOne();
    }

    /** DELETE /api/cache - clears every cache (to re-demo cache misses). */
    @DeleteMapping("/cache")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> clearCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
        return Map.of("cleared", cacheManager.getCacheNames());
    }
}
