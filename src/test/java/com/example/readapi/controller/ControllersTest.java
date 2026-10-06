package com.example.readapi.controller;

import com.example.readapi.dto.Dtos.Analytics;
import com.example.readapi.dto.Dtos.NPlusOneComparison;
import com.example.readapi.dto.Dtos.PageResponse;
import com.example.readapi.dto.Dtos.PerfResult;
import com.example.readapi.dto.Dtos.PostSummary;
import com.example.readapi.dto.Dtos.TopPost;
import com.example.readapi.service.AnalyticsService;
import com.example.readapi.service.PerformanceService;
import com.example.readapi.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllersTest {

    @Mock
    private AnalyticsService analyticsService;
    @Mock
    private PerformanceService performanceService;
    @Mock
    private PostService postService;
    @Mock
    private CacheManager cacheManager;

    @Test
    void analyticsControllerDelegatesCachedAndUncachedRequests() {
        Analytics analytics = new Analytics(1, 2, 3, 3.0, List.of(), Instant.EPOCH, 1);
        AnalyticsController controller = new AnalyticsController(analyticsService);
        when(analyticsService.getAnalytics()).thenReturn(analytics);
        when(analyticsService.computeAnalytics()).thenReturn(analytics);

        assertSame(analytics, controller.getAnalytics());
        assertSame(analytics, controller.getAnalyticsUncached());
        verify(analyticsService).getAnalytics();
        verify(analyticsService).computeAnalytics();
    }

    @Test
    void performanceControllerDelegatesComparisonAndClearsAvailableCaches() {
        PerformanceController controller = new PerformanceController(performanceService, cacheManager);
        NPlusOneComparison comparison = new NPlusOneComparison(1,
                new PerfResult("lazy", 2, 3, 1), new PerfResult("join", 1, 2, 1), "better");
        when(performanceService.compareNPlusOne()).thenReturn(comparison);
        Cache first = mock(Cache.class);
        Cache second = mock(Cache.class);
        when(cacheManager.getCacheNames()).thenReturn(Set.of("posts", "analytics"));
        when(cacheManager.getCache("posts")).thenReturn(first);
        when(cacheManager.getCache("analytics")).thenReturn(second);

        assertSame(comparison, controller.nPlusOne());
        assertEquals(Set.of("posts", "analytics"), controller.clearCaches().get("cleared"));
        verify(first).clear();
        verify(second).clear();
    }

    @Test
    void postControllerClampsTopPostLimitAndDelegatesPageRequests() {
        PostController controller = new PostController(postService);
        TopPost topPost = new TopPost(1L, "Popular", 10, 20, "Author");
        PageResponse<PostSummary> page = new PageResponse<>(List.of(), 0, 10, 0, 0, true, true, "UNSORTED");
        when(postService.getTopPosts(50)).thenReturn(List.of(topPost));
        when(postService.getPosts(any())).thenReturn(page);

        assertEquals(List.of(topPost), controller.getTop(100));
        assertSame(page, controller.getPosts(PageRequest.of(0, 10)));
        verify(postService).getTopPosts(50);
        verify(postService).getPosts(any());
    }
}
