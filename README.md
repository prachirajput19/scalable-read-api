# Scalable Read APIs with Caching & Optimization (Unit 2 - Experiment 6)

Spring Boot 3 + Spring Data JPA + Ehcache 3 (JCache) + H2 (in-memory, nothing to install).
Seeds 20 authors, 2000 posts and ~7000 comments on startup.

## Run (Windows / PowerShell)
Requirements: JDK 25+ and Maven 3.9+ (or open the folder in IntelliJ / VS Code and run `ReadApiApplication`).

```powershell
cd scalable-read-api
mvn spring-boot:run
```
Open http://localhost:8080 for the dashboard. H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:readdb`, user `sa`, empty password).

## Endpoints
| Method | URL | Demonstrates |
|---|---|---|
| GET | `/api/posts?page=0&size=10&sort=likes,desc` | 6.1 Pageable + sorting, indexed columns, DTO excerpts |
| GET | `/api/posts/feed?page=0&size=10` | 6.1 Slice (no COUNT query) for feeds |
| GET | `/api/posts/author/{id}` | 6.2 JPQL query (`@Query`) |
| GET | `/api/posts/all` | 6.2 `@Cacheable("posts")` (Ehcache) |
| GET | `/api/posts/top?limit=5` | 6.2 Native SQL query + cache |
| GET | `/api/analytics` / `/api/analytics/uncached` | 6.2 `@Cacheable("analytics")` vs baseline |
| GET | `/api/perf/n-plus-one` | 6.2 N+1 problem vs `JOIN FETCH` (SQL statement counts + time) |
| POST | `/api/posts` | Write path with `@CacheEvict` |
| DELETE | `/api/cache` | Clear all caches |

Every `/api` response carries an `X-Response-Time-Ms` header.

```powershell
Invoke-RestMethod "http://localhost:8080/api/posts?page=0&size=5&sort=likes,desc"
Invoke-RestMethod "http://localhost:8080/api/perf/n-plus-one"
Invoke-RestMethod -Method Post http://localhost:8080/api/posts -ContentType "application/json" `
  -Body '{"title":"Hello","content":"My first post","authorId":1}'
```

## Benchmarking
1. PowerShell (no extra tools): `powershell -ExecutionPolicy Bypass -File .\benchmark\benchmark.ps1`
2. Apache JMeter: open `benchmark/read-api-test-plan.jmx`, press Start, read the *Summary Report* (response time, throughput, error %). Or headless: `jmeter -n -t benchmark\read-api-test-plan.jmx -l results.jtl`

Expected: `/api/analytics` ~500 ms on the first call and a few ms afterwards (cache hit); the N+1 endpoint shows ~2000 statements vs 1 with JOIN FETCH.

## Structure
```
src/main/java/com/example/readapi
  controller/   PostController, AnalyticsController, PerformanceController
  service/      PostService, AnalyticsService, PerformanceService
  repository/   PostRepository (Pageable, JPQL, JOIN FETCH, native SQL), ...
  entity/       Author, Post (indexed), Comment
  dto/          Dtos (PageResponse, SliceResponse, PostSummary, Analytics, ...)
  config/       DataSeeder, ResponseTimeFilter
  exception/    GlobalExceptionHandler
src/main/resources  application.properties, ehcache.xml, static/index.html
```
