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

## Deploy API to Render and dashboard to Vercel

Vercel serves the static dashboard; the Spring Boot API runs as a long-lived service on Render.

1. Push this project to GitHub.
2. In Render, choose **New > Blueprint**, connect the repository, and deploy the `scalable-read-api` service using `render.yaml`. Copy its public service URL.
3. In Vercel, choose **Add New > Project**, import the same repository, and deploy from its root. Vercel uses `vercel.json` and the dependency-free Node build script to publish the dashboard.
4. In the Vercel project settings, add `API_BASE_URL` with the Render service URL (for example, `https://your-service.onrender.com`) for Production, Preview, and Development as appropriate, then redeploy.
5. In Render's service environment settings, set `APP_CORS_ALLOWED_ORIGINS` to the exact Vercel deployment origin (for example, `https://your-project.vercel.app`; do not include a path or trailing slash), then redeploy the API. Add any additional preview/custom-domain origins as comma-separated values; origins must match exactly.
6. Open the Vercel URL. The dashboard calls the Render API; API routes are not executed by Vercel.

The Docker image builds and runs the API with Java 25. It listens on Render's `PORT` environment variable and defaults to port 8080 locally. H2 is in-memory, so demo data is reseeded after each service restart; use a managed database for persistent production data.

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
