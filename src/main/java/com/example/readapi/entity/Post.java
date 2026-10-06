package com.example.readapi.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Indexes on the columns used for sorting and filtering (backend performance tuning). */
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_created_at", columnList = "created_at"),
        @Index(name = "idx_posts_likes", columnList = "likes"),
        @Index(name = "idx_posts_author", columnList = "author_id")
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private int likes;

    @Column(nullable = false)
    private int views;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    protected Post() {}

    public Post(String title, String content, int likes, int views, Instant createdAt, Author author) {
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.views = views;
        this.createdAt = createdAt;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public int getViews() { return views; }
    public Instant getCreatedAt() { return createdAt; }
    public Author getAuthor() { return author; }
    public List<Comment> getComments() { return comments; }
}
