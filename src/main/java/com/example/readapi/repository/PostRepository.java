package com.example.readapi.repository;

import com.example.readapi.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    /** Pagination + sorting. The entity graph loads the author in the same query (no N+1 on authors). */
    @Override
    @EntityGraph(attributePaths = "author")
    Page<Post> findAll(Pageable pageable);

    /** Slice = no COUNT(*) query; ideal for infinite-scroll feeds. */
    @EntityGraph(attributePaths = "author")
    Slice<Post> findSliceBy(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    List<Post> findAllByOrderByCreatedAtDesc();

    /** JPQL query using the entity model. */
    @Query(value = "SELECT p FROM Post p WHERE p.author.id = :authorId",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.author.id = :authorId")
    @EntityGraph(attributePaths = "author")
    Page<Post> findPostsByAuthor(@Param("authorId") Long authorId, Pageable pageable);

    /** N+1 solution: one SQL statement loads posts and their comments. */
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments")
    List<Post> findAllWithComments();

    /** Native SQL: columns are returned as Object[] (id, title, likes, views, author name). */
    @Query(value = "SELECT p.id, p.title, p.likes, p.views, a.name "
            + "FROM posts p JOIN authors a ON a.id = p.author_id "
            + "ORDER BY p.likes DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopPostRows(@Param("limit") int limit);

    @Query("SELECT COALESCE(SUM(p.likes), 0) FROM Post p")
    long sumLikes();

    /** Aggregation for the analytics dashboard: [authorName, postCount, totalLikes]. */
    @Query("SELECT a.name, COUNT(p), COALESCE(SUM(p.likes), 0) FROM Post p JOIN p.author a "
            + "GROUP BY a.id, a.name ORDER BY SUM(p.likes) DESC")
    List<Object[]> topAuthors(Pageable pageable);
}
