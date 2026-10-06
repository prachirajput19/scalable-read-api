package com.example.readapi.service;

import com.example.readapi.dto.Dtos.Analytics;
import com.example.readapi.repository.CommentRepository;
import com.example.readapi.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(postRepository, commentRepository);
        ReflectionTestUtils.setField(service, "simulatedDelayMs", 0L);
    }

    @Test
    void computesCountsRoundedAverageAndTopAuthorStats() {
        when(postRepository.count()).thenReturn(3L);
        when(commentRepository.count()).thenReturn(2L);
        when(postRepository.sumLikes()).thenReturn(10L);
        when(postRepository.topAuthors(PageRequest.of(0, 5)))
                .thenReturn(Collections.singletonList(new Object[]{"Ada", 2L, 8L}));

        Analytics result = service.computeAnalytics();

        assertEquals(3, result.totalPosts());
        assertEquals(2, result.totalComments());
        assertEquals(10, result.totalLikes());
        assertEquals(3.33, result.avgLikesPerPost());
        assertEquals("Ada", result.topAuthors().get(0).authorName());
        assertEquals(2, result.topAuthors().get(0).posts());
        assertNotNull(result.computedAt());
        verify(postRepository).topAuthors(PageRequest.of(0, 5));
    }

    @Test
    void usesZeroAverageWhenThereAreNoPosts() {
        when(postRepository.count()).thenReturn(0L);
        when(commentRepository.count()).thenReturn(0L);
        when(postRepository.sumLikes()).thenReturn(0L);
        when(postRepository.topAuthors(PageRequest.of(0, 5))).thenReturn(Collections.emptyList());

        assertEquals(0.0, service.computeAnalytics().avgLikesPerPost());
    }
}
