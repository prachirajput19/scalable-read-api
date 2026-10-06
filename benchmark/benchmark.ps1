# Simple load test (no JMeter needed). Run while the app is running:
#   powershell -ExecutionPolicy Bypass -File .\benchmark\benchmark.ps1
param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$Requests = 50
)

$endpoints = @(
    @{ Name = "Paged posts (page+count)";   Path = "/api/posts?page=0&size=10&sort=createdAt,desc" },
    @{ Name = "Feed (slice, no count)";     Path = "/api/posts/feed?page=0&size=10" },
    @{ Name = "Posts by author (JPQL)";     Path = "/api/posts/author/1?page=0&size=10" },
    @{ Name = "Top posts (native, cached)"; Path = "/api/posts/top?limit=5" },
    @{ Name = "All posts (cached)";         Path = "/api/posts/all" },
    @{ Name = "Analytics (uncached)";       Path = "/api/analytics/uncached" },
    @{ Name = "Analytics (cached)";         Path = "/api/analytics" }
)

Write-Host "Clearing caches..." -ForegroundColor Cyan
$null = Invoke-RestMethod -Method Delete -Uri "$BaseUrl/api/cache"

$results = foreach ($e in $endpoints) {
    $times = New-Object System.Collections.Generic.List[double]
    $errors = 0
    $sw = [System.Diagnostics.Stopwatch]::new()
    $total = [System.Diagnostics.Stopwatch]::StartNew()
    for ($i = 0; $i -lt $Requests; $i++) {
        $sw.Restart()
        try {
            $null = Invoke-WebRequest -Uri ($BaseUrl + $e.Path) -UseBasicParsing
            $sw.Stop()
            $times.Add($sw.Elapsed.TotalMilliseconds)
        } catch {
            $errors++
        }
    }
    $total.Stop()
    if ($times.Count -gt 0) {
        $sorted = $times | Sort-Object
        $p95 = $sorted[[math]::Max(0, [math]::Ceiling(0.95 * $sorted.Count) - 1)]
        [pscustomobject]@{
            Endpoint       = $e.Name
            "First(ms)"    = [math]::Round($times[0], 1)
            "Avg(ms)"      = [math]::Round(($times | Measure-Object -Average).Average, 1)
            "Min(ms)"      = [math]::Round($sorted[0], 1)
            "P95(ms)"      = [math]::Round($p95, 1)
            "Max(ms)"      = [math]::Round($sorted[-1], 1)
            "Req/s"        = [math]::Round($times.Count / $total.Elapsed.TotalSeconds, 1)
            "Errors(%)"    = [math]::Round(100.0 * $errors / $Requests, 1)
        }
    }
}
$results | Format-Table -AutoSize
