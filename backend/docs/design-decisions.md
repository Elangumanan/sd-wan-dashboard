# Design Decisions

This document records the key architectural choices made during the build, the rationale behind each, and what would change in a production system.

---

## 1. In-Memory Mock Data with `@PostConstruct`

**Decision:** All data is stored in `LinkedHashMap` instances inside a single `@Component` (`MockDataStore`). Data is seeded once at startup via `@PostConstruct`.

**Why:**
- The assignment requires deterministic data with no database setup
- `LinkedHashMap` preserves insertion order — the API returns items in the same order as the seed, every time
- `@PostConstruct` runs after Spring DI is complete, so all dependencies are available when seeding

**Immutability:** All public accessors return `Collections.unmodifiableMap(...)` or `List.copyOf(...)`. Repositories cannot modify the store.

**In production this would be:** JPA/Hibernate with a relational database (PostgreSQL). Repositories would extend `JpaRepository<T, ID>`. `MockDataStore` and the `InMemory*Repository` classes would be deleted.

---

## 2. Repository Interfaces with In-Memory Implementations

**Decision:** Every data access method is defined on an interface (`OrganizationRepository`, `SiteRepository`, etc.) with a corresponding `InMemory*` implementation in `repository/impl/`.

**Why:**
- Enforces the Dependency Inversion Principle — services depend on interfaces, not implementations
- Swapping to JPA requires only new `@Repository` implementations; service code is untouched
- Testability — service unit tests mock the repository interfaces, not the `MockDataStore`

**Trade-off:** More boilerplate for a mock project. Justified because the assignment explicitly asks for layered architecture.

---

## 3. `SuccessResponse<T>` Envelope for All 2xx Responses

**Decision:** Every controller returns `ResponseEntity<SuccessResponse<T>>`. The JSON body is always `{ "value": <T>, "message": "..." }`.

**Why:**
- Consistent response shape for frontend consumers regardless of endpoint
- The optional `message` field supports future use cases (e.g. "Site created successfully")
- Avoids the ambiguity of sometimes returning arrays at the root and sometimes objects

**Trade-off:** Frontend code must access `.value` instead of the root object/array. The Angular `SdwanApiService` unwraps the envelope internally so all downstream consumers receive the domain type directly.

**`@JsonInclude(NON_NULL)` on `message`:** The `message` field is omitted from JSON when absent — API responses for read operations are clean without an explicit `"message": null`.

---

## 4. `AppException` + `ExceptionFactory` Pattern

**Decision:** All domain exceptions extend `AppException` (which carries `ErrorCode` + `HttpStatus`). `ExceptionFactory` is the only place in `src/main` that instantiates them.

**Why:**
- A single `@ExceptionHandler(AppException.class)` in `GlobalExceptionHandler` handles every domain exception — adding new exception types requires no handler changes
- `ErrorCode` owns the HTTP status mapping — changing a status code requires editing one line in one enum
- `ExceptionFactory` enforces consistent message formats across all callers
- Prevents `new ResourceNotFoundException(...)` scattered across service code with inconsistent messages

**Ownership chain validation:** `DeviceServiceImpl.resolveDevice()` validates `org → site → device` before returning data. This prevents cross-hierarchy leakage (requesting a device from the wrong site returns a 404, not the device).

---

## 5. Java 17 Immutable Classes (not Records)

**Decision:** Domain models and DTOs are `final` classes with `private final` fields, all-args constructors, and record-style accessors (`id()` not `getId()`). Records were explicitly avoided.

**Why:** Project requirement — "We use Java 17. Convert all the records to class."

**Java 17 features used:**
- **Pattern matching for `instanceof`** in `equals()` methods — `if (!(o instanceof Organization other)) return false;`
- `Objects.requireNonNull()` in constructors
- `Objects.hash()` / `Objects.equals()` for `hashCode()` / `equals()`
- `List.copyOf()` for defensive copying of `List<>` fields in DTOs
- Switch expressions (`case HEALTHY -> ...`) in service aggregation logic

**Jackson serialization:** `JacksonConfig` sets field visibility to `ANY` and disables getter detection globally. This allows Jackson to serialize `private final` fields without `getXxx()` methods.

---

## 6. Dashboard as a Dedicated Aggregation Layer

**Decision:** The dashboard endpoints (`/api/dashboard/*`) live in a separate `DashboardController` + `DashboardService` that aggregate data from `SiteRepository` and `DeviceRepository` directly. They do **not** call `SiteService` or `OrganizationService`.

**Why:**
- The Overview page needs data from multiple entities simultaneously; calling 4+ domain APIs from the frontend wastes round-trips
- Aggregation logic (summing health counts, online/offline counts) is UI-concern code that belongs in a dedicated service, not in domain services
- `DashboardServiceImpl.getOverview()` calls `getSiteHealthSnapshot()` internally — all sites are fetched once, not twice

**Trade-off:** Health computation logic is duplicated between `SiteServiceImpl.computeHealth()` and `DashboardServiceImpl.computeHealth()`. Both are 3-line private methods with identical logic — duplication is acceptable at this scope.

---

## 7. CORS Global Filter

**Decision:** A global `CorsFilter` bean in `CorsConfig` handles CORS for all `/api/**` routes. The per-controller `@CrossOrigin(origins = "*")` annotation was removed.

**Why:**
- A single configuration point is easier to maintain than annotations on every controller
- `CorsFilter` runs earlier in the filter chain than `@CrossOrigin`, avoiding Spring MVC's late-stage CORS processing
- `addAllowedOriginPattern("*")` is used instead of `addAllowedOrigin("*")` — the latter is incompatible with `allowCredentials(true)` if that is ever needed

---

## 8. Deterministic WAN Traffic Data

**Decision:** WAN bandwidth history is pre-generated using a sine-wave formula with no random seed. The same 288 data points are produced on every run.

**Formula:**
```
factor(i) = (1 + sin(2π · i / 288 − π/2)) / 2    →  0.0 at i=0 (midnight), 1.0 at i=144 (noon)
rxMbps(i) = baseRx + ampRx · factor(i)
```

**Why:**
- The assignment requirement: "Backend data must be deterministic (same response on every call) — no random seeds at runtime"
- Sine-wave mimics real business-hours traffic patterns (low at night, peak midday)
- Pre-generated at startup and stored in unmodifiable lists — query-time cost is zero (a slice of a pre-built list)

**Fixed base timestamp:** `2026-05-30T00:00:00Z`. All WAN data is anchored to this point so the same timestamps are returned on every call.

---

## 9. WAN-Only Telemetry on the Device Page

**Decision:** The `/wan-history` endpoint filters out LAN interfaces and returns only WAN interface time-series. LAN interface current traffic is shown on the device detail page but has no history endpoint.

**Why:** The assignment is explicit — "Keep the device telemetry WAN-centric. LAN interface data can exist, but the assignment should clearly emphasize WAN monitoring."

---

## 10. OpenAPI / Swagger UI

**Decision:** `springdoc-openapi-starter-webmvc-ui` (v2.6.0) is added as a dependency. `OpenApiConfig` provides metadata. Controllers use `@Tag` and `@Operation` annotations.

**URLs:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

**Why:** The assignment asks for API documentation. Springdoc generates it automatically from the controller annotations with zero runtime overhead.

---

## What Would Change in Production

| Concern | Mock API | Production |
|---|---|---|
| Persistence | In-memory `LinkedHashMap` | PostgreSQL + JPA/Hibernate |
| Repositories | `InMemory*` implementations | Spring Data JPA repositories |
| Authentication | None | JWT / OAuth2 Bearer token |
| Data freshness | Fixed `@PostConstruct` seed | Live device telemetry via streaming |
| WAN history | Pre-computed sine wave | Time-series database (e.g. InfluxDB) |
| CORS | `allowAllOrigins("*")` | Whitelist of known frontend origins |
| Logging | SLF4J console | Structured JSON to log aggregator |
| Error monitoring | Console `ERROR` log | APM tool (Datadog, New Relic) |
| Health endpoint | Static `Map` response | Spring Actuator (`/actuator/health`) |
