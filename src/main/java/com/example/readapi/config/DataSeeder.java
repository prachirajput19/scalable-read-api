package com.example.readapi.config;

import com.example.readapi.entity.Author;
import com.example.readapi.entity.Comment;
import com.example.readapi.entity.Post;
import com.example.readapi.repository.AuthorRepository;
import com.example.readapi.repository.CommentRepository;
import com.example.readapi.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Generates a realistic dataset on startup so pagination, caching and N+1 are measurable. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String[] FIRST = {"Aarav", "Diya", "Rohan", "Ananya", "Kabir", "Ishita", "Vivaan", "Meera",
            "Arjun", "Saanvi", "Kiran", "Neha", "Rahul", "Priya", "Aditya", "Sneha", "Dev", "Tanya", "Nikhil", "Riya"};
    private static final String[] LAST = {"Sharma", "Verma", "Gupta", "Singh", "Kapoor", "Mehta", "Rao", "Nair",
            "Chopra", "Bansal"};
    private static final String[] TOPICS = {"Spring Boot tips", "Caching strategies", "Database indexing",
            "REST API design", "Pagination patterns", "Query tuning", "Microservices", "Load testing",
            "Dashboards", "Data pipelines"};
    private static final String[] SENTENCES = {
            "Efficient read paths matter more than raw write speed in feed based applications.",
            "Pagination keeps memory usage predictable and response times low.",
            "Always measure before optimizing and benchmark after every change.",
            "Caching hot data reduces database load and improves throughput.",
            "Fetching related entities lazily can silently trigger hundreds of extra queries.",
            "Indexes on sort and filter columns turn table scans into fast lookups.",
            "Smaller payloads travel faster, so return only the fields the client needs."};

    private final AuthorRepository authorRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Value("${app.seed.authors:20}")
    private int authorCount;

    @Value("${app.seed.posts:2000}")
    private int postCount;

    public DataSeeder(AuthorRepository authorRepository, PostRepository postRepository,
                      CommentRepository commentRepository) {
        this.authorRepository = authorRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void run(String... args) {
        if (authorRepository.count() > 0) {
            return;
        }
        long start = System.currentTimeMillis();
        Random rnd = new Random(42);
        Instant now = Instant.now();

        List<Author> authors = new ArrayList<>();
        for (int i = 1; i <= authorCount; i++) {
            authors.add(new Author(FIRST[i % FIRST.length] + " " + LAST[(i * 7) % LAST.length],
                    "author" + i + "@example.com"));
        }
        authorRepository.saveAll(authors);

        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= postCount; i++) {
            StringBuilder content = new StringBuilder();
            int sentences = 2 + rnd.nextInt(5);
            for (int s = 0; s < sentences; s++) {
                content.append(SENTENCES[rnd.nextInt(SENTENCES.length)]).append(' ');
            }
            int likes = rnd.nextInt(5000);
            int views = likes * 3 + rnd.nextInt(2000);
            Instant createdAt = now.minus(rnd.nextInt(90 * 24 * 60), ChronoUnit.MINUTES);
            posts.add(new Post("#" + i + " " + TOPICS[rnd.nextInt(TOPICS.length)],
                    content.toString().trim(), likes, views, createdAt, authors.get(rnd.nextInt(authors.size()))));
        }
        postRepository.saveAll(posts);

        List<Comment> comments = new ArrayList<>();
        for (Post post : posts) {
            int n = rnd.nextInt(8);
            for (int c = 0; c < n; c++) {
                comments.add(new Comment(SENTENCES[rnd.nextInt(SENTENCES.length)],
                        FIRST[rnd.nextInt(FIRST.length)] + " " + LAST[rnd.nextInt(LAST.length)],
                        post.getCreatedAt().plus(rnd.nextInt(600), ChronoUnit.MINUTES), post));
            }
        }
        commentRepository.saveAll(comments);

        log.info("Seeded {} authors, {} posts, {} comments in {} ms",
                authors.size(), posts.size(), comments.size(), System.currentTimeMillis() - start);
    }
}
