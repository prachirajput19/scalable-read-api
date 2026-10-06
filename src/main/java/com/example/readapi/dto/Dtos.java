package com.example.readapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/** All response/request shapes. Cached values implement Serializable (Ehcache via JCache stores by value). */
public final class Dtos {

    private Dtos() {}

    public record PostSummary(Long id, String title, String excerpt, int likes, int views,
                              Instant createdAt, Long authorId, String authorName) implements Serializable {}

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements,
                                  int totalPages, boolean first, boolean last, String sort) {
        public static <T> PageResponse<T> of(Page<T> p) {
            return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(),
                    p.getTotalPages(), p.isFirst(), p.isLast(), p.getSort().toString());
        }
    }

    public record SliceResponse<T>(List<T> content, int page, int size, boolean hasNext, String sort) {
        public static <T> SliceResponse<T> of(Slice<T> s) {
            return new SliceResponse<>(s.getContent(), s.getNumber(), s.getSize(), s.hasNext(), s.getSort().toString());
        }
    }

    public record CreatePostRequest(@NotBlank @Size(max = 200) String title,
                                    @NotBlank @Size(max = 2000) String content,
                                    @NotNull Long authorId) {}

    public record PostCommentCount(Long id, String title, int commentCount) {}

    public record TopPost(Long id, String title, int likes, int views, String authorName) implements Serializable {}

    public record AuthorStat(String authorName, long posts, long likes) implements Serializable {}

    public record Analytics(long totalPosts, long totalComments, long totalLikes, double avgLikesPerPost,
                            List<AuthorStat> topAuthors, Instant computedAt, long computeMillis) implements Serializable {}

    public record PerfResult(String strategy, long sqlStatements, long millis, int rows) {}

    public record NPlusOneComparison(int totalPosts, PerfResult naive, PerfResult joinFetch, String verdict) {}
}
