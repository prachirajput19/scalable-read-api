package com.example.readapi.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    protected Author() {}

    public Author(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Instant getJoinedAt() { return joinedAt; }
}
