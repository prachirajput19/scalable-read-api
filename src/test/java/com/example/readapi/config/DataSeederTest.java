package com.example.readapi.config;

import com.example.readapi.repository.AuthorRepository;
import com.example.readapi.repository.CommentRepository;
import com.example.readapi.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;

    private DataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DataSeeder(authorRepository, postRepository, commentRepository);
        ReflectionTestUtils.setField(seeder, "authorCount", 2);
        ReflectionTestUtils.setField(seeder, "postCount", 3);
    }

    @Test
    void seedsAuthorsPostsAndCommentsWhenDatabaseIsEmpty() {
        when(authorRepository.count()).thenReturn(0L);

        seeder.run("test");

        verify(authorRepository).saveAll(anyList());
        verify(postRepository).saveAll(anyList());
        verify(commentRepository).saveAll(anyList());
    }

    @Test
    void skipsSeedingWhenAuthorsAlreadyExist() {
        when(authorRepository.count()).thenReturn(1L);

        seeder.run();

        verify(authorRepository, never()).saveAll(anyList());
        verifyNoInteractions(postRepository, commentRepository);
    }
}
