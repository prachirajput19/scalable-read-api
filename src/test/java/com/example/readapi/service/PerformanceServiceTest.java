package com.example.readapi.service;

import com.example.readapi.dto.Dtos.NPlusOneComparison;
import com.example.readapi.dto.Dtos.PostCommentCount;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    private PostService postService;
    @Mock
    private EntityManagerFactory entityManagerFactory;
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Statistics statistics;

    @Test
    void comparesQueryCountsAndReportsResultSizes() {
        when(entityManagerFactory.unwrap(SessionFactory.class)).thenReturn(sessionFactory);
        when(sessionFactory.getStatistics()).thenReturn(statistics);
        when(statistics.getPrepareStatementCount()).thenReturn(3L, 1L);
        when(postService.commentCountsNPlusOne()).thenReturn(List.of(
                new PostCommentCount(1L, "post", 2)));
        when(postService.commentCountsJoinFetch()).thenReturn(List.of(
                new PostCommentCount(1L, "post", 2)));
        PerformanceService service = new PerformanceService(postService, entityManagerFactory);

        NPlusOneComparison result = service.compareNPlusOne();

        assertEquals(1, result.totalPosts());
        assertEquals(3, result.naive().sqlStatements());
        assertEquals(1, result.joinFetch().sqlStatements());
        assertEquals(1, result.naive().rows());
        assertTrue(result.verdict().contains("instead of 3"));
        verify(statistics, times(2)).clear();
        verify(postService, times(2)).commentCountsNPlusOne();
        verify(postService, times(2)).commentCountsJoinFetch();
    }
}
