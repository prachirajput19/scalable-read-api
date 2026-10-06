package com.example.readapi.service;

import com.example.readapi.dto.Dtos.*;
import com.example.readapi.entity.Author;
import com.example.readapi.entity.Post;
import com.example.readapi.exception.ResourceNotFoundException;
import com.example.readapi.repository.AuthorRepository;
import com.example.readapi.repository.PostRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;

    public PostService(PostRepository postRepository, AuthorRepository authorRepository) {
        this.postRepository = postRepository;
        this.authorRepository = authorRepository;
    }

    /** 6.1 - paginated + sorted read (includes total count). */
    public PageResponse<PostSummary> getPosts(Pageable pageable) {
        return PageResponse.of(postRepository.findAll(pageable).map(PostService::toSummary));
    }

    /** 6.1 - feed read using Slice: skips the COUNT query. */
    public SliceResponse<PostSummary> getFeed(Pageable pageable) {
        return SliceResponse.of(postRepository.findSliceBy(pageable).map(PostService::toSummary));
    }

    /** 6.1 - JPQL query filtered by author. */
    public PageResponse<PostSummary> getPostsByAuthor(Long authorId, Pageable pageable) {
        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author " + authorId + " not found");
        }
        return PageResponse.of(postRepository.findPostsByAuthor(authorId, pageable).map(PostService::toSummary));
    }

    /** 6.2 - Ehcache: first call hits the DB, later calls are served from memory. */
    @Cacheable(cacheNames = "posts", key = "'all'")
    public List<PostSummary> getAllPosts() {
        List<PostSummary> result = new ArrayList<>();
        for (Post p : postRepository.findAllByOrderByCreatedAtDesc()) {
            result.add(toSummary(p));
        }
        return result;
    }

    /** 6.2 - native SQL + cache. */
    @Cacheable(cacheNames = "topPosts", key = "#limit")
    public List<TopPost> getTopPosts(int limit) {
        List<TopPost> result = new ArrayList<>();
        for (Object[] r : postRepository.findTopPostRows(limit)) {
            result.add(new TopPost(((Number) r[0]).longValue(), (String) r[1],
                    ((Number) r[2]).intValue(), ((Number) r[3]).intValue(), (String) r[4]));
        }
        return result;
    }

    /** 6.2 - the N+1 problem: 1 query for posts + 1 query per post for its comments. */
    public List<PostCommentCount> commentCountsNPlusOne() {
        List<PostCommentCount> result = new ArrayList<>();
        for (Post p : postRepository.findAll()) {
            result.add(new PostCommentCount(p.getId(), p.getTitle(), p.getComments().size()));
        }
        return result;
    }

    /** 6.2 - the fix: JOIN FETCH loads posts and comments with a single query. */
    public List<PostCommentCount> commentCountsJoinFetch() {
        List<PostCommentCount> result = new ArrayList<>();
        for (Post p : postRepository.findAllWithComments()) {
            result.add(new PostCommentCount(p.getId(), p.getTitle(), p.getComments().size()));
        }
        return result;
    }

    /** Writes invalidate cached reads so clients never see stale feeds. */
    @Transactional
    @CacheEvict(cacheNames = {"posts", "topPosts", "analytics"}, allEntries = true)
    public PostSummary createPost(CreatePostRequest req) {
        Author author = authorRepository.findById(req.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author " + req.authorId() + " not found"));
        Post saved = postRepository.save(new Post(req.title(), req.content(), 0, 0, Instant.now(), author));
        return toSummary(saved);
    }

    /** Response-size optimization: return an excerpt instead of the full content. */
    static PostSummary toSummary(Post p) {
        String c = p.getContent();
        String excerpt = c.length() > 120 ? c.substring(0, 120) + "..." : c;
        return new PostSummary(p.getId(), p.getTitle(), excerpt, p.getLikes(), p.getViews(),
                p.getCreatedAt(), p.getAuthor().getId(), p.getAuthor().getName());
    }
}
