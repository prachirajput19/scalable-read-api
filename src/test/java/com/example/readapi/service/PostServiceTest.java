package com.example.readapi.service;

import com.example.readapi.dto.Dtos.*;
import com.example.readapi.entity.Author;
import com.example.readapi.entity.Comment;
import com.example.readapi.entity.Post;
import com.example.readapi.exception.ResourceNotFoundException;
import com.example.readapi.repository.AuthorRepository;
import com.example.readapi.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private AuthorRepository authorRepository;

    private PostService service;
    private Author author;
    private Post post;

    @BeforeEach
    void setUp() {
        service = new PostService(postRepository, authorRepository);
        author = new Author("Ada", "ada@example.com");
        post = new Post("Title", "short body", 5, 9, Instant.EPOCH, author);
    }

    @Test
    void mapsPageAndSliceReadsToResponseDtos() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(postRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(post), pageable, 1));
        when(postRepository.findSliceBy(pageable)).thenReturn(new SliceImpl<>(List.of(post), pageable, false));

        assertEquals("short body", service.getPosts(pageable).content().get(0).excerpt());
        assertEquals("Title", service.getFeed(pageable).content().get(0).title());
    }

    @Test
    void rejectsUnknownAuthorsBeforeQueryingPosts() {
        when(authorRepository.existsById(9L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getPostsByAuthor(9L, PageRequest.of(0, 5)));
        verifyNoInteractions(postRepository);
    }

    @Test
    void mapsTopRowsAndReturnsAllPostSummaries() {
        when(postRepository.findTopPostRows(2))
                .thenReturn(List.<Object[]>of(new Object[]{7L, "top", 12, 20, "Ada"}));
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(post));

        assertEquals("top", service.getTopPosts(2).get(0).title());
        assertEquals(12, service.getTopPosts(2).get(0).likes());
        assertEquals("Title", service.getAllPosts().get(0).title());
    }

    @Test
    void createsPostForExistingAuthorAndCountsComments() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postRepository.findAll()).thenReturn(List.of(post));
        when(postRepository.findAllWithComments()).thenReturn(List.of(post));
        post.getComments().add(new Comment("comment", "Grace", Instant.EPOCH, post));

        PostSummary created = service.createPost(new CreatePostRequest("New", "Content", 1L));
        List<PostCommentCount> naive = service.commentCountsNPlusOne();
        List<PostCommentCount> fetched = service.commentCountsJoinFetch();

        assertEquals("New", created.title());
        assertEquals(1, naive.get(0).commentCount());
        assertEquals(1, fetched.get(0).commentCount());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createsExcerptAtTheConfiguredLength() {
        String content = "x".repeat(121);
        Post longPost = new Post("Long", content, 0, 0, Instant.EPOCH, author);

        assertEquals("x".repeat(120) + "...", PostService.toSummary(longPost).excerpt());
        assertEquals("short body", PostService.toSummary(post).excerpt());
    }
}
