package com.example.readapi.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_post", columnList = "post_id")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String body;

    @Column(name = "commenter_name", nullable = false)
    private String commenterName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    protected Comment() {}

    public Comment(String body, String commenterName, Instant createdAt, Post post) {
        this.body = body;
        this.commenterName = commenterName;
        this.createdAt = createdAt;
        this.post = post;
    }

    public Long getId() { return id; }
    public String getBody() { return body; }
    public String getCommenterName() { return commenterName; }
    public Instant getCreatedAt() { return createdAt; }
    public Post getPost() { return post; }
}
