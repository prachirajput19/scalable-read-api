package com.example.readapi;

import com.example.readapi.entity.Author;
import com.example.readapi.entity.Comment;
import com.example.readapi.entity.Post;
import com.example.readapi.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationAndEntityTest {

    @Test
    void applicationEnablesSpringBootAndCaching() {
        assertNotNull(ReadApiApplication.class.getAnnotation(SpringBootApplication.class));
        assertNotNull(ReadApiApplication.class.getAnnotation(EnableCaching.class));
    }

    @Test
    void entitiesExposeConstructorValuesAndInitializeCollections() {
        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        Author author = new Author("Ada Lovelace", "ada@example.com");
        Post post = new Post("Title", "Body", 12, 34, createdAt, author);
        Comment comment = new Comment("Nice post", "Grace", createdAt, post);

        assertEquals("Ada Lovelace", author.getName());
        assertEquals("ada@example.com", author.getEmail());
        assertNotNull(author.getJoinedAt());
        assertEquals("Title", post.getTitle());
        assertEquals("Body", post.getContent());
        assertEquals(12, post.getLikes());
        assertEquals(34, post.getViews());
        assertEquals(createdAt, post.getCreatedAt());
        assertSame(author, post.getAuthor());
        assertNotNull(post.getComments());
        assertTrue(post.getComments().isEmpty());
        assertEquals("Nice post", comment.getBody());
        assertEquals("Grace", comment.getCommenterName());
        assertEquals(createdAt, comment.getCreatedAt());
        assertSame(post, comment.getPost());
    }

    @Test
    void notFoundExceptionPreservesMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("missing");

        assertEquals("missing", exception.getMessage());
    }
}
