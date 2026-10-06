package com.example.readapi.service;

import com.example.readapi.dto.Dtos.Analytics;
import com.example.readapi.dto.Dtos.AuthorStat;
import com.example.readapi.repository.CommentRepository;
import com.example.readapi.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Value("${app.analytics.simulated-delay-ms:500}")
    private long simulatedDelayMs;

    public AnalyticsService(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    /** Cached dashboard data: heavy aggregation runs once, then served from Ehcache. */
    @Cacheable(cacheNames = "analytics", key = "'dashboard'")
    public Analytics getAnalytics() {
        return computeAnalytics();
    }

    /** Same computation without the cache (used as the benchmark baseline). */
    public Analytics computeAnalytics() {
        long start = System.nanoTime();
        long posts = postRepository.count();
        long comments = commentRepository.count();
        long likes = postRepository.sumLikes();

        List<AuthorStat> top = new ArrayList<>();
        for (Object[] r : postRepository.topAuthors(PageRequest.of(0, 5))) {
            top.add(new AuthorStat((String) r[0], ((Number) r[1]).longValue(), ((Number) r[2]).longValue()));
        }

        try {
            Thread.sleep(simulatedDelayMs); // simulates an expensive report
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        double avg = posts == 0 ? 0 : Math.round((double) likes / posts * 100.0) / 100.0;
        long millis = (System.nanoTime() - start) / 1_000_000;
        return new Analytics(posts, comments, likes, avg, top, Instant.now(), millis);
    }
}
