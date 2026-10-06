package com.example.readapi.repository;

import com.example.readapi.entity.Author;
import com.example.readapi.entity.Comment;
import com.example.readapi.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RepositoriesTest {

    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;

    @Test
    void repositoriesPersistEntitiesAndRunPostQueries() {
        Author author = authorRepository.save(new Author("Ada", "ada@example.com"));
        Post older = postRepository.save(new Post("Older", "body", 4, 20,
                Instant.parse("2025-01-01T00:00:00Z"), author));
        Post newer = postRepository.save(new Post("Newer", "body", 9, 30,
                Instant.parse("2025-02-01T00:00:00Z"), author));
        commentRepository.save(new Comment("comment", "Grace",
                Instant.parse("2025-02-02T00:00:00Z"), newer));

        assertTrue(authorRepository.existsById(author.getId()));
        assertEquals(1, commentRepository.count());

        Page<Post> page = postRepository.findAll(PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "createdAt")));
        assertEquals(2, page.getTotalElements());
        assertEquals("Newer", page.getContent().get(0).getTitle());
        assertEquals(2, postRepository.findPostsByAuthor(author.getId(), PageRequest.of(0, 10)).getTotalElements());
        assertEquals(2, postRepository.findSliceBy(PageRequest.of(0, 10)).getNumberOfElements());
        assertEquals("Newer", postRepository.findAllByOrderByCreatedAtDesc().get(0).getTitle());
        assertEquals(2, postRepository.findAllWithComments().size());
        assertEquals(13, postRepository.sumLikes());
        assertEquals("Newer", postRepository.findTopPostRows(1).get(0)[1]);
        List<Object[]> topAuthors = postRepository.topAuthors(PageRequest.of(0, 5));
        assertEquals("Ada", topAuthors.get(0)[0]);
        assertEquals(2L, ((Number) topAuthors.get(0)[1]).longValue());
        assertEquals(13L, ((Number) topAuthors.get(0)[2]).longValue());
        assertNotNull(older.getId());
    }
}
