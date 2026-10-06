package com.example.readapi.dto;

import com.example.readapi.dto.Dtos.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtosTest {

    @Test
    void mapsPageAndSliceMetadata() {
        PageRequest pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "title"));
        PageResponse<String> page = PageResponse.of(new PageImpl<>(List.of("a", "b"), pageable, 4));
        SliceResponse<String> slice = SliceResponse.of(new SliceImpl<>(List.of("a", "b"), pageable, true));

        assertEquals(List.of("a", "b"), page.content());
        assertEquals(1, page.page());
        assertEquals(2, page.size());
        assertEquals(4, page.totalElements());
        assertEquals(2, page.totalPages());
        assertFalse(page.first());
        assertTrue(page.last());
        assertEquals("title: DESC", page.sort());
        assertEquals(List.of("a", "b"), slice.content());
        assertEquals(1, slice.page());
        assertEquals(2, slice.size());
        assertTrue(slice.hasNext());
        assertEquals("title: DESC", slice.sort());
    }

    @Test
    void requestRecordDeclaresValidationConstraints() throws Exception {
        var title = CreatePostRequest.class.getDeclaredMethod("title");
        var content = CreatePostRequest.class.getDeclaredMethod("content");
        var authorId = CreatePostRequest.class.getDeclaredMethod("authorId");

        assertNotNull(title.getAnnotation(NotBlank.class));
        assertEquals(200, title.getAnnotation(Size.class).max());
        assertNotNull(content.getAnnotation(NotBlank.class));
        assertEquals(2000, content.getAnnotation(Size.class).max());
        assertNotNull(authorId.getAnnotation(NotNull.class));
    }

    @Test
    void responseRecordsRetainValues() {
        PostSummary summary = new PostSummary(1L, "title", "excerpt", 2, 3, null, 4L, "author");
        Analytics analytics = new Analytics(1, 2, 3, 3.0, List.of(new AuthorStat("author", 1, 3)), null, 5);

        assertEquals("excerpt", summary.excerpt());
        assertEquals(1, analytics.totalPosts());
        assertEquals("author", analytics.topAuthors().get(0).authorName());
        assertEquals("verdict", new NPlusOneComparison(0, null, null, "verdict").verdict());
    }
}
