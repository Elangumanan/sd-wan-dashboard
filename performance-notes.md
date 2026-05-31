# Performance Discussion Notes

This document covers the key performance tradeoffs relevant to this SD-WAN dashboard, organised around the four areas listed in the assignment checklist: frontend rendering, Angular state, API design, and UX.

---

## 1. Frontend Rendering

### Current implementation

The WAN history chart renders all data points returned by the API directly through Chart.js. For the default 6-hour range this is 72 points — well within what Chart.js handles without degradation. The 24-hour range sends 288 points, which is still fast but sits at the practical ceiling before rendering becomes noticeably slower on low-end hardware.

`pointRadius: 0` is already set, which removes per-point DOM overhead and is the single biggest rendering win for dense time-series.

### What breaks at scale

The optional `mock-data/chart-api-datasets.json` file can generate datasets in the 100k–200k point range. At that scale:

- **Canvas pixel saturation** — a 600px-wide canvas has 600 horizontal pixels. Drawing 100k points collapses many points onto the same pixel, wasting CPU on invisible work.
- **JavaScript heap pressure** — Chart.js keeps the full dataset in memory for tooltip hit-testing even when points are invisible.

### Recommended improvements

**Largest win — server-side downsampling by resolution:**  
Return coarser data for wider time windows. For example:

| Range | Points today | Target | Interval |
|---|---|---|---|
| 1h | 12 | 12 | 5 min (no change) |
| 6h | 72 | 72 | 5 min (no change) |
| 24h | 288 | 96 | 15 min |
| 7d | — | 168 | 1 hour |
| 30d | — | 180 | 4 hours |

This is a backend-only change; the frontend is already coded to handle whatever slice the API returns.

**Client-side LTTB (Largest Triangle Three Buckets):**  
For cases where the API cannot be changed, LTTB is the standard algorithm for downsampling time-series while preserving visual shape. A lightweight (~1 KB) JS implementation reduces a 10k-point series to 200 visually representative points in under 5 ms.

**Avoid re-processing on every render:**  
Downsampled or formatted data should be computed once when the API response arrives and stored in a signal. The current `wanStats` `computed()` signal already does this — it runs once per new `wanHistory` value, not on every change-detection cycle.

---

## 2. Angular State

### Current implementation

After removing NgRx, each component owns its data via `signal()` and `computed()`. API calls are made directly in `ngOnInit` using `forkJoin` for parallel requests (dashboard) or sequential calls (device page).

### What works well

- `computed()` for derived data (`wanStats`, `sortedSites`, `wanInterfaces`, `lanInterfaces`) runs lazily — Angular only re-evaluates when an upstream signal changes, not on every change-detection pass.
- `forkJoin` on the dashboard page fires both API calls in parallel, cutting initial load time roughly in half compared to sequential calls.
- Signals integrate with Angular's fine-grained change detection — only the DOM nodes that read a changed signal re-render.

### What breaks at scale

**No caching between navigations.** Every time the user visits the device page they pay the full round-trip for device detail + WAN history, even if they navigated away and came back 10 seconds later. With `signal()` living inside the component, state is destroyed on destroy.

**No de-duplication of in-flight requests.** If the user changes the time range twice quickly, two overlapping requests are in flight. The second may resolve before the first, causing a stale update.

### Recommended improvements

**Route-level caching with a service:**  
Move `signal()` state into a `@Injectable({ providedIn: 'root' })` service keyed by `deviceId + range`. On navigation, check the cache first; only call the API if the entry is absent or stale (e.g. > 60 seconds old). This pattern is trivially implemented with a `Map<string, { data, fetchedAt }>`.

**`switchMap` for range changes:**  
Wrap the range-change API call in `switchMap` so that a newer request automatically cancels the previous in-flight one:

```typescript
private readonly rangeChange$ = new Subject<WanHistoryRange>();

ngOnInit(): void {
  this.rangeChange$.pipe(
    switchMap(range => this.api.getWanHistory(orgId, siteId, deviceId, range))
  ).subscribe(history => this.wanHistory.set(history));
}
```

---

## 3. API Design

### Current implementation

| Endpoint | Payload content |
|---|---|
| `GET /api/dashboard/overview` | Aggregated counts only — no raw device list |
| `GET /api/dashboard/site-health` | Per-site snapshot — no device detail |
| `GET /api/organizations/:id/sites` | Site list items only — no device detail |
| `GET /api/.../devices/:id` | Full device detail including interfaces |
| `GET /api/.../wan-history?range=6h` | Server-side sliced to 72 points |

The key design decision was a **dedicated dashboard aggregation layer** (`DashboardController`). The frontend makes two calls to get the overview page instead of fetching every org → site → device chain and computing counts on the client. This is the most impactful API design choice in the project.

WAN history uses **numeric Unix millisecond timestamps**, which are more compact than ISO-8601 strings and parse faster in JavaScript (`new Date(ts)` vs. `Date.parse(str)`).

### What could be improved

**Multiple resolution endpoints:**  
Rather than a single `?range=` parameter, expose resolution as a separate concern:

```
GET /api/.../wan-history?range=24h&resolution=15m
```

This lets the client request a 24-hour overview at 15-minute granularity (96 points) without the backend hardcoding the mapping.

**Field projection:**  
The site list endpoint returns `onlineDeviceCount` and `offlineDeviceCount` for every site. A larger deployment might return dozens of extra fields the overview page does not use. A `?fields=id,name,health,deviceCount` query parameter pattern avoids over-fetching without versioning the endpoint.

**Payload compression:**  
At 288 points × 3 fields (timestamp, rxMbps, txMbps) × 8 bytes, a single WAN interface 24h history is ~7 KB of JSON. Enabling gzip on the Spring Boot response (already the default when `Accept-Encoding: gzip` is sent) reduces this to ~1–2 KB with no code change.

---

## 4. UX

### Current implementation

- All four pages show an explicit loading state while data is fetching.
- Error states display a human-readable message if an API call fails (the HTTP interceptor also surfaces a toast notification automatically).
- Empty states are handled on the site table and device table (no sites / no devices messages).
- The time-range dropdown lets users narrow from 24h → 6h → 1h, reducing both payload size and chart density on demand.

### What to add next

**Skeleton screens instead of spinners:**  
Replace `<p>Loading…</p>` with placeholder shimmer blocks that match the shape of the content. Users perceive skeleton screens as faster because the layout does not shift when data arrives.

**Stale-while-revalidate pattern:**  
Show the previous cached data immediately on re-navigation, then silently re-fetch in the background and update only if the new data differs. This eliminates the loading flash on repeat visits.

**Chart loading guard:**  
The WAN chart currently renders a blank canvas while history is loading. A simple "Fetching WAN data…" overlay over the chart area communicates intent and avoids a jarring canvas-pop when data arrives.

**Pre-selecting the narrowest useful range:**  
The default is 6 hours. If the device was last seen within the past hour, default to 1h. This reduces payload size automatically for the most common case.

---

## Summary: Biggest Wins by Effort

| Win | Effort | Impact |
|---|---|---|
| Server-side range slicing (already done) | Low | High |
| `forkJoin` for parallel requests (already done) | Low | Medium |
| `computed()` for derived data (already done) | Low | Medium |
| Route-level cache service | Medium | High |
| `switchMap` for range de-duplication | Low | Medium |
| LTTB client downsampling | Medium | High (at scale) |
| Resolution query param on WAN history | Medium | High (at scale) |
| Skeleton screens | Medium | Medium (perceived perf) |
