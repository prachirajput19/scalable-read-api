package com.example.readapi.service;

import com.example.readapi.dto.Dtos.NPlusOneComparison;
import com.example.readapi.dto.Dtos.PerfResult;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

/** Measures SQL statement count + elapsed time using Hibernate statistics. */
@Service
public class PerformanceService {

    private final PostService postService;
    private final Statistics statistics;

    public PerformanceService(PostService postService, EntityManagerFactory emf) {
        this.postService = postService;
        this.statistics = emf.unwrap(SessionFactory.class).getStatistics();
    }

    public NPlusOneComparison compareNPlusOne() {
        // warm-up so JIT / connection pool do not skew the first measurement
        postService.commentCountsNPlusOne();
        postService.commentCountsJoinFetch();

        PerfResult naive = measure("Lazy loading (N+1 problem)", postService::commentCountsNPlusOne);
        PerfResult fetch = measure("JOIN FETCH (single query)", postService::commentCountsJoinFetch);

        String verdict = String.format("JOIN FETCH used %d SQL statement(s) instead of %d (%.0fx fewer) and was %s.",
                fetch.sqlStatements(), naive.sqlStatements(),
                (double) naive.sqlStatements() / Math.max(1, fetch.sqlStatements()),
                fetch.millis() <= naive.millis()
                        ? (naive.millis() - fetch.millis()) + " ms faster"
                        : "similar in speed on this small in-memory dataset");
        return new NPlusOneComparison(naive.rows(), naive, fetch, verdict);
    }

    private PerfResult measure(String label, Supplier<List<?>> action) {
        statistics.clear();
        long start = System.nanoTime();
        List<?> rows = action.get();
        long millis = (System.nanoTime() - start) / 1_000_000;
        return new PerfResult(label, statistics.getPrepareStatementCount(), millis, rows.size());
    }
}
