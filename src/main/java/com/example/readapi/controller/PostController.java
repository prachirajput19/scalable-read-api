package com.example.readapi.controller;

import com.example.readapi.dto.Dtos.*;
import com.example.readapi.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /** GET /api/posts?page=0&size=10&sort=likes,desc */
    @GetMapping
    public PageResponse<PostSummary> getPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.getPosts(pageable);
    }

    /** GET /api/posts/feed - Slice based (no COUNT query). */
    @GetMapping("/feed")
    public SliceResponse<PostSummary> getFeed(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.getFeed(pageable);
    }

    /** GET /api/posts/author/3?page=0&size=5 - JPQL query. */
    @GetMapping("/author/{authorId}")
    public PageResponse<PostSummary> getByAuthor(
            @PathVariable Long authorId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.getPostsByAuthor(authorId, pageable);
    }

    /** GET /api/posts/top?limit=5 - native SQL, cached. */
    @GetMapping("/top")
    public List<TopPost> getTop(@RequestParam(defaultValue = "5") int limit) {
        return postService.getTopPosts(Math.min(Math.max(limit, 1), 50));
    }

    /** GET /api/posts/all - every post, cached with Ehcache. */
    @GetMapping("/all")
    public List<PostSummary> getAll() {
        return postService.getAllPosts();
    }

    /** POST /api/posts - create a post and evict cached reads. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostSummary create(@Valid @RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }
}
