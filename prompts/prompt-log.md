# Prompt Log

Use this file to document meaningful AI interactions during the assignment.

## Entry Template

### Date

`YYYY-MM-DD`

### Goal

Short statement of what you were trying to achieve.

### Exact Prompt

```text
Paste the exact prompt here.
```

### Files / Context Provided

- `path/to/file`
- `path/to/other-file`

### Output Summary

Short summary of what AI returned.

### What I Kept

- Item kept from the AI response

### What I Changed Manually

- Manual improvement or correction

### What AI Missed Or Got Wrong

- Missing edge case
- Weak design choice
- Incorrect assumption

### Reusable?

`yes` or `no`, with a short reason.

---

## Entry 1 — Requirements Analysis

### Date

`2026-05-31`

### Goal

Analyse `instruction.md` and produce a structured report covering business requirements, functional requirements, and technical requirements before any code is written.

### Exact Prompt

```text
Analyze the provided instruction.md file.

Tasks:
1. Summarize the business requirements.
2. Identify functional requirements.
3. Identify the technical requirements

Do not generate code.
Produce only the analysis report.
```

### Files / Context Provided

- `instruction.md`

### Output Summary

AI produced a three-section report: business summary (3-level SD-WAN monitoring dashboard), functional requirements (navigation, site health rules, WAN telemetry, error/loading states, prompt logging), and technical requirements (Spring Boot 3 / Java 17 backend, Angular 17 strict-mode frontend, all HTTP through SdwanApiService, performance considerations).

### What I Kept

- Full three-section structure used as the authoritative requirements reference for implementation
- Site health rule table (HEALTHY / DEGRADED / DOWN)
- API endpoint list derived from the analysis

### What I Changed Manually

- None — report was used as-is for planning

### What AI Missed Or Got Wrong

- Did not explicitly call out that the `range` query parameter must be validated server-side
- Performance checklist was listed as "optional" but is worth addressing in the WAN chart implementation

### Reusable?

`yes` — the prompt pattern (read a spec file, produce structured analysis without generating code) is a reliable first step for any new feature or module.

---

## Entry 2 — Backend Design and Implementation

### Date

`2026-05-31`

### Goal

Design and implement the full Spring Boot 3 backend: layered architecture (Controller → Service → Repository), in-memory mock data, domain models, DTOs, exception handling, OpenAPI docs, and unit tests.

### Exact Prompt

```text
Help me to design and develop this requirement. As per instruction, need to log all the prompts used in this requirement.
So whatever discussing here should be log as entry in prompt/prompt-log.md file.

Step 2: Design the backend features and perform the basic project setup.

Features:
- Set up REST API endpoints.
- Set up the service layer.
- Set up an in-memory database (refer to the project requirements/instructions).
- Implement an exception factory and global exception handling using @ControllerAdvice.
- Define the domain models and DTOs.
- Configure application properties and project structure.
- Add request validation and error response handling.
- Create repository/data access components for the in-memory database.
- Add unit tests for controllers and services.
- Provide API documentation using OpenAPI/Swagger.

Requirements:
1. Well-structured Spring Boot 3 backend project.
2. Layered architecture (Controller → Service → Repository).
3. Follow the SOLID principle.
```

### Files / Context Provided

- `instruction.md`
- `backend/pom.xml`
- `backend/src/main/java/com/example/sdwan/api/SdwanController.java`
- `backend/src/main/resources/application.properties`

### Output Summary

AI implemented the complete backend from scratch:
- 4 enums, 5 domain records, 10 DTOs
- `MockDataStore` — `@PostConstruct` singleton seeding 1 org, 4 sites, 12 devices, 28 interfaces, 288-point WAN history per WAN interface using deterministic sine-wave formula
- 5 repository interfaces + 5 in-memory implementations
- 3 service interfaces + 3 service implementations (OrganizationService delegates health aggregation to SiteService; SiteService computes health from device counts; DeviceService handles ownership-chain validation)
- 3 REST controllers with full path hierarchy `/api/organizations/{orgId}/sites/{siteId}/devices/{deviceId}`
- `GlobalExceptionHandler` (@RestControllerAdvice) covering 404, 400 (validation + type mismatch), 500
- `CorsConfig` (global filter replacing per-controller @CrossOrigin)
- `OpenApiConfig` + springdoc-openapi-starter-webmvc-ui 2.6.0 dependency
- 5 unit tests (2 service, 3 controller) with MockMvc and Mockito

### What I Kept

- Full layered architecture as designed
- MockDataStore approach (single @Component with @PostConstruct, maps as in-memory store)
- Sine-wave formula for deterministic WAN traffic: `(1 + sin(2π·i/288 − π/2)) / 2` — business-hours pattern, low at midnight, peak at noon
- Hierarchical path ownership validation in DeviceServiceImpl (org → site → device chain check)
- `computeHealth` as a package-visible static method on SiteServiceImpl (testable without Spring context)

### What I Changed Manually

- Replaced varargs + reflection `put()` helper in MockDataStore with plain `for-each` over `List.of(...)` — simpler and avoids reflection at startup
- Removed redundant `import java.util.List` after wildcard import was already present

### What AI Missed Or Got Wrong

- Maven not on PATH in the dev environment — build could not be verified automatically; requires `mvn` to be added to PATH or invoked via full path
- DeviceService unit test was not generated (only OrganizationService and SiteService tests written)

### Reusable?

`yes` — the layered architecture prompt (Controller → Service → Repository + in-memory store + @PostConstruct seeding + GlobalExceptionHandler) is a reliable template for any Spring Boot mock-API assignment.

---

## Entry 3 — Centralised Exception Handling

### Date

`2026-05-31`

### Goal

Implement centralised exception handling using `@ControllerAdvice`, covering all required exception types, consistent error response format, structured field-level validation details, and logging.

### Exact Prompt

```text
Implement centralized exception handling using Spring Boot's @ControllerAdvice.

Requirements:
1. Create a global exception handler to intercept and process all application exceptions.
2. Map exceptions to appropriate HTTP status codes.
3. Ensure all error responses conform to the standard error response format.
4. Handle common exception types such as:
   * ResourceNotFoundException
   * ValidationException
   * IllegalArgumentException
   * Generic RuntimeException
   * Unexpected/Internal Server Errors
```

### Files / Context Provided

- `exception/GlobalExceptionHandler.java`
- `exception/ResourceNotFoundException.java`
- `dto/ErrorResponseDto.java`

### Output Summary

AI delivered:
- New `ValidationException` class with two constructors: `(message)` and `(field, message)` for field-scoped errors
- Extended `ErrorResponseDto` with a `details: List<String>` field (backwards-compatible via a delegating 5-param constructor)
- Full rewrite of `GlobalExceptionHandler` covering 11 exception types across 404 / 405 / 400 / 500 ranges, with SLF4J logging (WARN for 4xx, ERROR for 5xx with full stack trace)
- New `GlobalExceptionHandlerTest` with 7 test cases exercising each handler path end-to-end through MockMvc

### What I Kept

- All 11 exception handlers as written
- `details[]` always present in response body (empty array for non-validation errors, populated for `MethodArgumentNotValidException` and `ConstraintViolationException`)
- Logging strategy: WARN for client errors, ERROR with stack trace for server faults
- Two `build()` overloads — one without details, one with — keeping each call site clean

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- `NoResourceFoundException` requires Spring Boot 3.2+ (`org.springframework.web.servlet.resource`); if the project is ever downgraded below 3.2, this import would need to be swapped for `NoHandlerFoundException` with the `spring.mvc.throw-exception-if-no-handler-found=true` property
- `ValidationException` and `IllegalArgumentException` map to the same status (400) — if a distinction between client-input errors and programmer-contract errors is needed in future, they should map to different sub-codes in the response

### Reusable?

`yes` — the handler template (all common Spring MVC exceptions + domain exceptions + logging strategy) is reusable across any Spring Boot 3 project with minimal changes.

---

## Entry 4 — Standardised API Response Models

### Date

`2026-05-31`

### Goal

Create generic `SuccessResponse<T>` and `ErrorResponse` models to standardise all API response shapes, introduce application-level error codes, and wire the models into controllers and the exception handler.

### Exact Prompt

```text
Analyze the project structure and create standardized API response models.

Requirements:
1. Create a generic SuccessResponse<T> model:
   { value: T, message: String // optional }

2. Create an ErrorResponse model:
   { code: String, message: String, details: Object // optional }

3. Follow Java and Spring Boot 3 best practices.
4. Place the classes in the appropriate package structure.
5. Provide the complete implementation with imports.
```

### Files / Context Provided

- `controller/OrganizationController.java`
- `controller/SiteController.java`
- `controller/DeviceController.java`
- `exception/GlobalExceptionHandler.java`
- `dto/ErrorResponseDto.java`
- All 4 test files under `src/test`

### Output Summary

AI delivered:
- New `com.example.sdwan.response` package with three classes: `ErrorCode` enum (10 codes), `ErrorResponse` (immutable, `@JsonInclude(NON_NULL)`, two static factories), `SuccessResponse<T>` (generic, immutable, `@JsonInclude(NON_NULL)`, two static factories)
- Deleted `ErrorResponseDto` — fully replaced by `ErrorResponse`
- Updated `GlobalExceptionHandler` to return `ErrorResponse` with typed `ErrorCode` values
- Updated all 3 controllers to return `ResponseEntity<SuccessResponse<T>>`
- Updated all 4 test files: success JSON paths shifted from `$.field` → `$.value.field`; error paths shifted from `$.status`/`$.error` → `$.code`
- Added `errorResponse_detailsAbsent_forSimpleErrors` test asserting `details` is omitted for non-validation errors

### What I Kept

- `details: Object` type (not `List<String>`) — allows structured payloads (maps, nested objects) for richer future error cases
- `@JsonInclude(NON_NULL)` at class level for both models — omits absent optional fields from JSON, keeping simple error responses minimal
- `ErrorCode` enum stores as `.name()` String internally — consumers get `"RESOURCE_NOT_FOUND"` not an ordinal; type safety lives in Java
- Static factory methods (`of(...)`) on both classes — cleaner call sites than constructors
- Pattern matching for `instanceof` in `equals()` — consistent with the rest of the codebase's Java 17 style

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- `SuccessResponse` wraps every response including list endpoints — the frontend must now read `response.value` instead of the root array. This is a breaking API contract change that the Angular service layer must account for when implemented
- No `SuccessResponse` wrapper was applied to the existing `GET /api/health` endpoint in `SdwanController`; that endpoint intentionally returns a raw `Map` and was left unchanged

### Reusable?

`yes` — the `SuccessResponse<T>` / `ErrorResponse` / `ErrorCode` pattern is a reusable template for any Spring Boot 3 project that needs a consistent API envelope without adding a full framework dependency.

---

## Entry 5 — Exception Factory

### Date

`2026-05-31`

### Goal

Implement a centralised `ExceptionFactory` using the Factory Method Pattern to eliminate hardcoded exception creation across the codebase, introduce `BusinessException` and `UnauthorizedException`, and make `ErrorCode` the single source of truth for code → HTTP status mapping.

### Exact Prompt

```text
Implement an Exception Factory using the Factory Method Pattern.

Requirements:
1. Create a centralized exception factory.
2. Avoid hardcoded exception creation throughout the application.
3. Provide factory methods for:
   * resourceNotFound()
   * validationError()
   * businessError()
   * unauthorized()
4. Ensure all exceptions use standardized error codes.
5. Generate complete implementation and package structure.
```

### Files / Context Provided

- `exception/GlobalExceptionHandler.java`
- `exception/ResourceNotFoundException.java`
- `exception/ValidationException.java`
- `response/ErrorCode.java`
- `service/impl/OrganizationServiceImpl.java`
- `service/impl/SiteServiceImpl.java`
- `service/impl/DeviceServiceImpl.java`

### Output Summary

AI delivered:
- `AppException` — abstract base class carrying `ErrorCode`; derives `HttpStatus` from it via `errorCode.httpStatus()`
- `BusinessException` (422) and `UnauthorizedException` (401) — two new domain exceptions
- `ResourceNotFoundException` and `ValidationException` updated to extend `AppException` instead of `RuntimeException`
- `ErrorCode` enhanced with `httpStatus()` method and two new codes: `BUSINESS_ERROR` / `UNAUTHORIZED`
- `ExceptionFactory` with 6 static factory methods as the sole instantiation point for all domain exceptions
- `GlobalExceptionHandler` simplified: three specific domain handlers replaced by a single `AppException` handler using `ResponseEntity` with dynamic status from `ex.httpStatus()`
- All three service implementations updated to call `ExceptionFactory.*` — `new ResourceNotFoundException(...)` eliminated from service code
- `ExceptionFactoryTest` (8 tests) verifying type, error code, HTTP status, and message for every factory method
- `GlobalExceptionHandlerTest` updated with tests for `businessError()` (422) and `unauthorized()` (401)

### What I Kept

- Abstract `AppException` as the base — forces use of specific subclasses, prevents generic `new AppException(...)` calls
- `ErrorCode` owns `HttpStatus` — one enum constant change updates both the error code string and the HTTP status in all handlers
- Single `AppException` handler in `GlobalExceptionHandler` — future subclasses are handled automatically without adding new handler methods
- `ExceptionFactory` as a `final` non-instantiable utility class — clean, no Spring context needed

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- `IllegalArgumentException` is still handled separately in `GlobalExceptionHandler` because it is a standard Java exception, not an `AppException` subclass — callers that throw `IllegalArgumentException` directly bypass the factory pattern; they should ideally throw `ExceptionFactory.validationError()` instead
- No `@ExceptionHandler` test for `BusinessException` with a `details` payload — only the simple (no-details) case was tested

### Reusable?

`yes` — the `AppException` + `ExceptionFactory` pattern is directly reusable in any Spring Boot 3 project. The factory is the only change required when adding a new exception type: add the subclass, add the factory method, the handler picks it up automatically.

---

## Entry 6 — Dashboard Overview APIs

### Date

`2026-05-31`

### Goal

Design and implement a dedicated dashboard aggregation layer with two endpoints — `/api/dashboard/overview` and `/api/dashboard/site-health` — that return UI-ready data for the Overview page without requiring the frontend to call multiple domain APIs.

### Exact Prompt

```text
Design and implement backend APIs for the Overview Dashboard page.

Dashboard Requirements:
1. Total Sites
2. Total Edge Devices
3. Sites by Status (Donut Chart)
4. Edge Devices by Status (Donut Chart)
5. Site Health Snapshot (Table): Site Name, Health Status, Total Devices, Online Devices, Offline Devices

API Design Requirements:
Create a dedicated dashboard aggregation layer that provides UI-ready data instead of
requiring the frontend to call multiple domain APIs.

API 1: GET /api/dashboard/overview
Response: Total Sites, Total Edge Devices, Site Status Summary, Edge Device Status Summary

API 2: GET /api/dashboard/site-health
Response: Site Name, Health Status, Total Devices, Online Devices, Offline Devices
```

### Files / Context Provided

- `repository/SiteRepository.java`
- `repository/impl/InMemorySiteRepository.java`
- `service/impl/SiteServiceImpl.java`
- Existing DTO and controller conventions

### Output Summary

AI delivered:
- `SiteRepository.findAll()` added (needed to query all sites cross-org)
- 4 new DTOs: `SiteStatusSummaryDto` (healthy/degraded/down + total), `DeviceStatusSummaryDto` (online/offline + total), `DashboardOverviewDto`, `SiteHealthSnapshotDto`
- `DashboardService` interface + `DashboardServiceImpl` — `getOverview()` calls `getSiteHealthSnapshot()` internally and aggregates with a switch expression, avoiding a second full pass over sites
- `DashboardController` with two `GET` endpoints, both returning `SuccessResponse<T>` and documented with Swagger `@Operation`
- `DashboardServiceTest` (6 tests) covering HEALTHY/DEGRADED/DOWN health rules, zero-device edge case, full aggregation correctness, and empty catalogue
- `DashboardControllerTest` (3 tests) covering 200 with all JSON fields, correct array length, and empty list

### What I Kept

- `getOverview()` delegates to `getSiteHealthSnapshot()` for aggregation — single site/device fetch, no duplication
- `total` computed in DTO constructors (`SiteStatusSummaryDto`, `DeviceStatusSummaryDto`) — frontend never has to sum the parts
- Switch expression in `DashboardServiceImpl.getOverview()` for health status counting — idiomatic Java 17
- Health rule inlined as `private static` in `DashboardServiceImpl` — the dashboard layer is self-contained and does not depend on `SiteServiceImpl`

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- No pagination or sorting on `GET /api/dashboard/site-health` — fine for the current mock data set (4 sites) but would need `Pageable` support before use with large production data
- Dashboard is scoped to all organisations in the store; if multi-org filtering by `orgId` is needed in future, the endpoints will need a `?orgId=` query parameter

### Reusable?

`yes` — the pattern of a dedicated aggregation service that calls `repository.findAll()` + a per-entity mapping method, then aggregates in the overview method, is a clean template for any summary dashboard endpoint.

---

## Entry 7 — Angular 17 Frontend Foundation

### Date

`2026-05-31`

### Goal

Set up the Angular 17 frontend foundation: lazy-loaded routing, NgRx state management with the dashboard feature wired end-to-end, a reusable Chart.js integration layer, a functional HTTP interceptor with centralised error handling, and a signal-based global toast notification system.

### Exact Prompt

```text
Initialize the Angular 17 foundation for an SD-WAN Dashboard.

Requirements:
* Configure application routing with lazy-loaded feature modules.
* Set up NgRx Store, Effects, and Selectors for API state management and caching.
* Create a reusable Chart.js integration layer.
* Add an HTTP interceptor for API requests and centralized error handling.
* Implement global exception handling with toast notifications.

Provide:
* Folder/package structure
* Dependency installation commands
* Configuration files
* Production-ready code examples
* Angular 17 best practices
```

### Files / Context Provided

- `frontend/package.json`
- `frontend/src/main.ts`
- `frontend/src/app/app.component.ts`
- `frontend/src/app/core/sdwan-api.service.ts`
- `frontend/src/app/core/models.ts`

### Output Summary

AI delivered:

**Install command:** `npm install @ngrx/store@17 @ngrx/effects@17 @ngrx/store-devtools@17 chart.js`

**24 TypeScript files created/updated:**
- 5 model files (`api-response`, `organization`, `site`, `device`, `dashboard`) matching backend `SuccessResponse<T>` / `ErrorResponse` shapes
- Updated `sdwan-api.service.ts` with all 8 API methods; each unwraps `SuccessResponse<T>` so callers receive the domain type directly
- `api.interceptor.ts` — functional interceptor attaching JSON headers + mapping HTTP errors to human-readable toast messages
- `toast.service.ts` — signal-based (`signal<Toast[]>`) with four severity levels and auto-dismiss timers
- `toast.component.ts` — uses Angular 17 `@for` control flow, fixed overlay with slide-in animation
- `donut-chart.component.ts` — Chart.js tree-shaken integration (registers only `DoughnutController`, `ArcElement`, `Tooltip`, `Legend`); handles `OnChanges` for live data updates
- NgRx dashboard store: `createActionGroup` actions, typed reducer, `createFeatureSelector` selectors (including two pre-shaped donut segment selectors), functional effects (`{ functional: true }`)
- `dashboard.routes.ts` — feature state (`provideState`) and effects (`provideEffects`) registered in route providers for true lazy initialisation
- `app.config.ts` — `ApplicationConfig` with `provideRouter(withPreloading)`, `provideHttpClient(withInterceptors)`, `provideStore({})`, `provideStoreDevtools`
- `app.component.ts` — minimal shell with sticky top-nav and `<router-outlet>`
- `main.ts` — delegates to `appConfig`

### What I Kept

- Functional effects (`{ functional: true }`) and `inject()` — Angular 17 best practice, no class boilerplate
- Feature state registered in route providers, not in root — state only exists when the user is on the dashboard page
- `SuccessResponse<T>` unwrapping inside `SdwanApiService` — consumers get typed domain objects, not API envelopes
- Interceptor shows toast AND rethrows — the HTTP error travels to NgRx effects which dispatch failure actions for store state tracking without double-notifying
- Chart.js tree-shaking — only registers the controllers actually used

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- No unit tests written for the Angular layer (interceptor, effects, selectors, service) — these should be added before calling the frontend done
- `app.component.css` still contains the starter shell styles; these should be cleaned up now that `AppComponent` is a minimal shell
- The organizations feature route points to `NotFoundComponent` as a placeholder — will be replaced when that feature is built

### Reusable?

`yes` — the `app.config.ts` + `app.routes.ts` + functional interceptor + signal-based toast pattern is a clean Angular 17 starter template reusable across any SPA project with a REST backend.

---

## Entry 8 — Fix chart.js Missing Dependency

### Date

`2026-05-31`

### Goal

Diagnose and fix a TypeScript build error `TS2307: Cannot find module 'chart.js'` that blocked `ng serve` in the Angular frontend.

### Exact Prompt

```text
In frontend, I have an Angular issue while running serve.
Here is the exact error:
TS2307: Cannot find module 'chart.js' or its corresponding type declarations. [plugin angular-compiler]

    src/app/shared/components/donut-chart/donut-chart.component.ts:18:7:
      18 │ } from 'chart.js';
         ╵        ~~~~~~~~~~

Here is the relevant code:
import {
  ArcElement,
  Chart,
  type ChartData,
  DoughnutController,
  Legend,
  Tooltip,
} from 'chart.js';

Explain root cause first, then propose the smallest safe fix.
```

### Files / Context Provided

- `frontend/package.json`
- `frontend/src/app/shared/components/donut-chart/donut-chart.component.ts`

### Output Summary

AI identified that `chart.js` was referenced in `donut-chart.component.ts` but was never added to `package.json`. The fix was a single install command: `npm install chart.js`. No code changes needed because `chart.js` v4+ ships its own bundled `.d.ts` files.

### What I Kept

- Diagnosis and fix as-is — single `npm install chart.js` resolved the error.

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — root cause was correct and the fix was minimal.

### Reusable?

`yes` — `TS2307` on a third-party package always means the package is not installed; checking `package.json` for the missing entry is the correct first diagnostic step.

---

## Entry 9 — Fix chart.js Generic Type Mismatch

### Date

`2026-05-31`

### Goal

Diagnose and fix a TypeScript `TS2322` error caused by a type mismatch between the `Chart` constructor's inferred return type and the narrower `Chart<'doughnut'>` field declaration.

### Exact Prompt

```text
I have a Spring Boot / Angular issue.
Here is the exact error:
TS2322: Type 'Chart<keyof ChartTypeRegistry, number[], unknown>' is not assignable to
type 'Chart<"doughnut", number[], unknown>'.
  Types of property 'config' are incompatible.
    ...
      Type 'keyof ChartTypeRegistry' is not assignable to type '"doughnut"'.

Here is the relevant code:
src/app/shared/components/donut-chart/donut-chart.component.ts:101:4

Explain root cause first, then propose the smallest safe fix.
```

### Files / Context Provided

- `frontend/src/app/shared/components/donut-chart/donut-chart.component.ts`

### Output Summary

AI identified that `new Chart(ctx, { type: 'doughnut', … })` returns `Chart<keyof ChartTypeRegistry>` because TypeScript cannot narrow the generic from a string literal inside a config object. The field `this.chart` was typed as `Chart<'doughnut'>`, making the assignment fail. Fix was adding the explicit type parameter: `new Chart<'doughnut'>(ctx, …)` on line 101 — a one-character change, no other files affected.

### What I Kept

- Fix as-is — `new Chart<'doughnut'>(ctx, …)` resolved the error immediately.

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — root cause and fix were both correct and minimal.

### Reusable?

`yes` — whenever `Chart<'doughnut'>` (or any Chart.js narrow type) is used as a field type, the constructor call must supply the explicit type parameter; TypeScript cannot infer it from the config object's `type` property.

---

## Entry 10 — Fix Implicit `any` Types in dashboard.selectors.ts

### Date

`2026-05-31`

### Goal

Resolve all TypeScript `any` type errors in `dashboard.selectors.ts` so that `npm run start` compiles cleanly under Angular strict mode.

### Exact Prompt

```text
Fix Angular compilation errors in `dashboard.selector.ts`.

Requirements:
- Resolve all TypeScript type errors, especially implicit `any` parameters.
- Preserve existing functionality and business logic.
- Do not refactor unrelated code.
- Use the most specific types available from existing models, interfaces, or NgRx state definitions.
- Ensure `npm run start` compiles successfully after the changes.

Output:
1. Root cause of each error.
2. Updated code.
3. Summary of changes.
```

### Files / Context Provided

- `frontend/src/app/features/dashboard/store/dashboard.selectors.ts`
- `frontend/src/app/features/dashboard/store/dashboard.reducer.ts`
- `frontend/src/app/store/app.state.ts`
- `frontend/src/app/core/models/dashboard.model.ts`

### Output Summary

AI identified three `any` usages across two selector projectors and the loading combiner:
- `overview: any` in `selectSiteDonutSegments` → `DashboardOverview | null`
- `overview: any` in `selectDeviceDonutSegments` → `DashboardOverview | null`
- `(a: any, b: any)` in `selectDashboardLoading` → `(a: boolean, b: boolean)`

Added the missing `DashboardOverview` import. Also scanned all other frontend source files (effects, actions, interceptor, service, component, models) and confirmed no further `any` usage exists.

### What I Kept

- All fixes as proposed — each type replaced with the most specific available type from existing interfaces.

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — all three errors were correctly identified and fixed with minimal changes.

### Reusable?

`yes` — when NgRx `createSelector` projector parameters show `any`, the correct type is always the output type of the input selector(s); checking the reducer's state interface gives the exact type.

---

## Entry 11 — Install Missing NgRx Dependencies

### Date

`2026-05-31`

### Goal

Identify and fix missing NgRx packages that prevented `npm run start` from resolving NgRx imports used throughout the dashboard feature.

### Exact Prompt

```text
NgRx library is missing
```

### Files / Context Provided

- `frontend/package.json`

### Output Summary

AI confirmed that `@ngrx/store`, `@ngrx/effects`, and `@ngrx/store-devtools` were entirely absent from `package.json`. Provided the correct install command pinned to `@17` to match the Angular 17 project: `npm install @ngrx/store@17 @ngrx/effects@17 @ngrx/store-devtools@17`.

### What I Kept

- Install command as given.

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — root cause (packages never installed) and fix (single install command) were both correct.

### Reusable?

`yes` — NgRx packages must always be pinned to the same major version as Angular; omitting them from `package.json` entirely is a common oversight when code is generated before dependencies are installed.

---

## Entry 12 — Overview Page UI — Match Reference Mockup

### Date

`2026-05-31`

### Goal

Rebuild the Overview dashboard page to closely match the reference mockup at `frontend/public/final_dashboard_mock/final-overview.png`, covering layout, sidebar navigation, stat cards, donut charts with custom legends, and the site health snapshot table.

### Exact Prompt

```text
Looks good. Use `frontend/public/final-overview.png` as the reference mockup for the Overview page.

Follow the mockup closely for:
- Layout and component structure
- Styling and visual design
- Spacing, alignment, and sizing
- Typography and colors
- Charts, tables, and card presentation
- Responsive behavior

Ensure the implementation matches the reference design as closely as possible while following
the project's existing Angular and styling standards.
```

### Files / Context Provided

- `frontend/public/final_dashboard_mock/final-overview.png` (reference mockup image)
- `frontend/src/app/app.component.ts`
- `frontend/src/app/app.routes.ts`
- `frontend/src/app/app.config.ts`
- `frontend/src/app/features/dashboard/dashboard.component.ts`
- `frontend/src/app/shared/components/donut-chart/donut-chart.component.ts`

### Output Summary

AI read the mockup image and made targeted changes to three files:

**`DonutChartComponent`:**
- Added `centerValue` input — renders large bold number in the donut center
- Updated center overlay to a flex-column `div` showing `centerValue` (large, dark) + `centerLabel` (small, muted) stacked
- Disabled Chart.js built-in legend so the dashboard can render a custom HTML legend to the right

**`AppComponent`:**
- Replaced top navigation bar with a sidebar + topbar shell layout
- Sidebar: dark navy `#1a2d4a`, brand text, 7 nav links with active highlight, FLOW indicator at bottom
- Topbar: white, "Operations Summary" title, two avatar circles (N / U)
- Page content scrolls inside a flex column

**`DashboardComponent`:**
- Page header: breadcrumb, large title, subtitle, "Last updated: Just now" aligned right
- 5 stat cards in a flex row: Total Sites, Total Edge Devices, Healthy (green), Degraded (orange), Down (red)
- 2 chart panels: donut on left + custom HTML legend on right showing `count (percentage%)`
- Site health snapshot table: blue clickable site names, dot-and-pill status badges, grey uppercase column headers
- `pct()` helper for percentage calculation (strips trailing `.0`)

### What I Kept

- All changes as proposed — full match to the mockup structure and visual hierarchy
- `pct()` helper moved into the component class (not a pipe) — simple and scoped to this view
- Custom legend as HTML rather than Chart.js built-in — matches the mockup's right-side layout and allows percentage display

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Sidebar nav links for Sites, Edge Devices, Alerts, Reports, Settings point to routes that do not yet exist — they will fall through to the 404 component until those features are built

### Reusable?

`yes` — the sidebar + topbar shell pattern (`AppComponent`) and the donut chart + right-side custom legend pattern are directly reusable for any multi-page Angular dashboard.

---

## Entry 13 — Reusable Tile Component

### Date

`2026-05-31`

### Goal

Create a reusable, accessible `TileComponent` for displaying stat cards with type-based colour accents, optional click behaviour, and a custom label colour override.

### Exact Prompt

```text
Implement reusable Angular (v17+) UI components following Angular best practices,
standalone components, strong TypeScript typing, accessibility standards, and responsive design.

1. Reusable Tile Component

Create a reusable TileComponent with the following requirements:

Inputs
  title: string
  count: number | string
  subtitle?: string
  clickable: boolean
  labelColor?: string
  type?: 'default' | 'success' | 'warning' | 'danger' | 'info'

Outputs
  tileClick event emitted when a clickable tile is selected.

Behavior
  Display title, count, and optional subtitle.
  Support different visual styles based on the tile type.
  Apply custom label color when provided.
  If clickable is enabled: pointer cursor, hover state, shadow/highlight, emit tileClick.
  If clickable is disabled: disable interaction and clickable styling.
```

### Files / Context Provided

- Existing shared components structure (`shared/components/`)

### Output Summary

AI created `src/app/shared/components/tile/tile.component.ts`:
- `TileType` union type exported alongside the component
- `title` and `count` marked `@Input({ required: true })` — Angular 17 compile-time guard
- Type accent via `border-left: 4px solid` + matching count colour for each of the 5 types
- `labelColor` overrides count colour via `[style.color]` binding
- Clickable: `role="button"`, `tabindex="0"`, `Enter`/`Space` keyboard handlers, hover lift + shadow, `focus-visible` ring
- Disabled: `aria-disabled="true"`, `opacity: .85`, `cursor: default`, click guard
- `tileClasses` getter builds the class string cleanly without template logic

### What I Kept

- Full implementation as proposed
- `@Input({ required: true })` on `title` and `count` — compile-time safety
- `focus-visible` ring instead of plain `:focus` — avoids outline on mouse click

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — all specified inputs, outputs, and behaviours were implemented correctly.

### Reusable?

`yes` — `TileComponent` is a general-purpose stat card; the `type` + `clickable` + `labelColor` combination covers virtually all dashboard KPI tile patterns without changes.

---

## Entry 14 — Replace Stat Cards with TileComponent in Dashboard

### Date

`2026-05-31`

### Goal

Replace the five hand-rolled `div.stat-card` elements in `DashboardComponent` with the newly created `TileComponent`, removing the duplicate CSS.

### Exact Prompt

```text
replace with tile components
```

### Files / Context Provided

- `frontend/src/app/features/dashboard/dashboard.component.ts`
- `frontend/src/app/shared/components/tile/tile.component.ts`

### Output Summary

AI made targeted changes to `DashboardComponent`:
- Added `TileComponent` to the `imports` array
- Replaced 5 `div.stat-card` blocks with 5 `<app-tile>` one-liners, mapping `type` to `default` / `success` / `warning` / `danger`
- Removed all `.stat-card`, `.stat-card__label`, `.stat-card__value` CSS rules
- Kept the `.stat-cards` flex container and added `app-tile { flex: 1 1 120px }` host sizing rule

### What I Kept

- All changes as proposed — clean replacement with no behaviour change.

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — mapping was straightforward and all dead CSS was removed.

### Reusable?

`yes` — the pattern of replacing local card markup with a shared `TileComponent` is the standard approach for any dashboard page that needs stat tiles.

---

## Entry 15 — Organization Details Page

### Date

`2026-05-31`

### Goal

Implement the full Organization Details page following the existing Angular/NgRx architecture, matching the reference mockup at `frontend/public/final_dashboard_mock/final-organization.png`.

### Exact Prompt

```text
Implement the Organization Details Page following the existing Angular architecture, routing,
NgRx patterns, reusable components, styling conventions, and design standards used throughout
the project.

Page Requirements
1. Organization Header — display org name from route parameter
2. Organization Summary Cards — Total Sites, Healthy Sites, Degraded Sites, Down Sites,
   Total Edge Devices using existing TileComponent
3. Sites Table — Site Name, Total Edge Devices, Status, Healthy, Degraded, Down columns
   with sorting, status badges, loading/error/empty states

Refer mock up in frontend/public/final-organization.png
```

### Files / Context Provided

- `frontend/public/final_dashboard_mock/final-organization.png` (reference mockup)
- `frontend/src/app/core/models/organization.model.ts`
- `frontend/src/app/core/models/site.model.ts`
- `frontend/src/app/features/organizations/organizations.routes.ts`
- `frontend/src/app/features/dashboard/store/dashboard.actions.ts` (pattern reference)
- `frontend/src/app/features/dashboard/store/dashboard.effects.ts` (pattern reference)
- `frontend/src/app/features/dashboard/dashboard.routes.ts` (pattern reference)

### Output Summary

AI created 7 files following the exact NgRx pattern from the dashboard feature:

**NgRx store (4 files):**
- `organization.actions.ts` — `createActionGroup` with 6 events for load org + load sites, each carrying `orgId`
- `organization.reducer.ts` — `OrganizationState` with `organization`, `sites[]`, `loading`, `error`
- `organization.selectors.ts` — 5 selectors; `selectTotalEdgeDevices` computed by summing `site.deviceCount` across all sites (since `OrganizationSummary` does not carry this field)
- `organization.effects.ts` — two functional effects (`loadOrganization$`, `loadSites$`) matching the dashboard pattern

**Components (2 files):**
- `organization-list.component.ts` — calls `getOrganizations()` directly (no store needed), auto-redirects to first org's detail route with `replaceUrl: true`
- `organization-detail.component.ts` — full page with header/breadcrumb, 5 `TileComponent` tiles, sortable sites table, loading/error/empty states

**Routes (1 file updated):**
- `organizations.routes.ts` — `''` → list redirect, `':orgId'` → detail with lazy `provideState` + `provideEffects`

**Also updated:**
- `app.state.ts` — added `OrganizationState` to `AppState` interface

**Key design decisions:**
- `healthyCount` / `degradedCount` / `downCount` helpers: since devices are only ONLINE/OFFLINE, device counts are derived from site health — e.g. `degradedCount = site.health === 'DEGRADED' ? site.offlineDeviceCount : 0` — matching the mockup values
- Client-side sorting with `▲`/`▼` indicators; `HEALTH_ORDER` map for status sort
- `totalEdgeDevices` derived in a selector from `sites.reduce(sum + deviceCount)`

### What I Kept

- Full implementation as proposed
- `HEALTH_ORDER` record for typed health-status sort (avoids string comparison)
- `replaceUrl: true` on the list redirect — back button skips the loading screen
- Lazy state registration in route providers — org state only lives while on `/organizations/:orgId`

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Site name links point to `/organizations/:orgId/sites/:siteId` which does not yet exist — will resolve to 404 until the Site detail page is implemented

### Reusable?

`yes` — the pattern (NgRx feature store + list-redirect component + detail component + lazy route providers) is a direct template for any subsequent detail page (Site, Device).

---

## Entry 16 — Refactor Components: Separate Files + Angular Signals

### Date

`2026-05-31`

### Goal

Refactor all Angular components from single-file inline style to the standard three-file structure (`.ts` / `.html` / `.scss`) and adopt Angular Signals wherever applicable, replacing `@Input`/`@Output`, `AsyncPipe`, `ngOnChanges`, and plain component properties.

### Exact Prompt

```text
Refactor the Angular components to follow Angular best practices and improve maintainability.

Requirements

1. Separate Component Files
   Move business logic to *.ts files.
   Move HTML structure to *.html files.
   Move component styles to *.scss files.
   Ensure each component follows Angular's standard file structure and conventions.

2. Adopt Angular Signals
   Use Angular Signals wherever applicable for component state management.
   Replace traditional state handling patterns (BehaviorSubject, simple component
   properties, unnecessary RxJS usage, etc.) with Signals when appropriate.
   Use computed() and effect() where they improve readability and performance.
   Follow Angular 17+ best practices for Signals.
```

### Files / Context Provided

All 8 components were read before refactoring:
- `app.component.ts`
- `features/dashboard/dashboard.component.ts`
- `features/organizations/organization-detail.component.ts`
- `features/organizations/organization-list.component.ts`
- `shared/components/donut-chart/donut-chart.component.ts`
- `shared/components/tile/tile.component.ts`
- `shared/components/toast/toast.component.ts`
- `shared/components/not-found/not-found.component.ts`

### Output Summary

AI created 16 new files (8 `.html` + 8 `.scss`) and updated all 8 `.ts` files to use `templateUrl` / `styleUrl`. Signal changes per component:

| Component | Signal changes |
|---|---|
| `TileComponent` | `@Input()` → `input()` / `input.required()`, `@Output()` → `output()`, `get tileClasses` → `computed()` |
| `DonutChartComponent` | `@Input()` → `input()`, `ngOnChanges` removed → `effect()` in constructor tracks `segments()` and calls `update()` |
| `DashboardComponent` | 5 store observables → `toSignal()`, `AsyncPipe` removed, template uses signal calls directly |
| `OrganizationDetailComponent` | `sortKey`/`sortDir` → `signal()`, `sortDir.update()` for toggle, store streams → `toSignal()`, `sorted()` method → `computed()` |
| `OrganizationListComponent` | `error: string \| null` → `signal<string \| null>()` |
| `AppComponent`, `ToastComponent`, `NotFoundComponent` | File split only — no mutable state to migrate |

SCSS uses BEM nesting (`&--modifier`, `&__element`) throughout.

### What I Kept

- All changes as proposed
- `effect()` in `DonutChartComponent` constructor with `if (this.chart)` guard — clean and avoids introducing a flag variable
- `sortDir.update(d => d === 'asc' ? 'desc' : 'asc')` — idiomatic signal update, clearer than a setter
- `toSignal()` with `{ initialValue: [] }` on array streams — avoids `undefined` in templates without null guards

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — all components were correctly split and signal adoption was complete.

### Reusable?

`yes` — the three-file split + `input()`/`output()`/`computed()`/`toSignal()` pattern is the Angular 17+ standard template for any new component going forward.

---

## Entry 17 — Site Detail Page

### Date

`2026-05-31`

### Goal

Implement the Site Detail page following existing Angular/NgRx architecture, matching the reference mockup at `frontend/public/final_dashboard_mock/final-site.png`.

### Exact Prompt

```text
Implement the site page using angular 17 components

Details:
1. header information
   1. Site name
   2. Site description
2. Tile information
   1. Total edge devices
   2. Online
   3. Offline
3. List of devices in table view
   Columns:
   1. Device name - In form of link
   2. Role
   3. Uptime
   4. Status
   5. System IP

Features:
On click of device name link in table, should navigate to device page.

Refer the mock up in public/frontend/final-site.png
```

### Files / Context Provided

- `frontend/public/final_dashboard_mock/final-site.png` (reference mockup)
- `frontend/src/app/core/models/site.model.ts`
- `frontend/src/app/core/models/device.model.ts`
- `frontend/src/app/features/organizations/organizations.routes.ts`
- `frontend/src/app/store/app.state.ts`

### Output Summary

AI created 7 files and updated 3 existing files:

**Model update:**
- `site.model.ts` — added optional `role?` and `uptime?` to `DeviceSummary` (backend may or may not return them; template falls back to `device.model` / `'—'`)

**NgRx store (4 files):**
- `site.actions.ts` — `loadSite` / `loadSiteSuccess` / `loadSiteFailure`, each carrying `orgId` + `siteId`
- `site.reducer.ts` — `SiteState` with `site`, `loading`, `error`
- `site.selectors.ts` — 6 selectors; `selectOnlineCount` / `selectOfflineCount` derived from the devices array
- `site.effects.ts` — functional effect calling `api.getSite(orgId, siteId)`

**Component (3 files):**
- `site-detail.component.ts` — reads `orgId` + `siteId` from route params, dispatches `loadSite`, all store streams → `toSignal()`
- `site-detail.component.html` — breadcrumb with links, title + inline health badge, 3 `TileComponent` tiles, devices table with device-name links to `/organizations/:orgId/sites/:siteId/devices/:deviceId`
- `site-detail.component.scss` — BEM SCSS matching mockup: breadcrumb, inline health badge, tile row, devices table, ONLINE/OFFLINE status badges

**Route wired in `organizations.routes.ts`:**
- Added `':orgId/sites/:siteId'` route with lazy `provideState('site')` + `provideEffects(SiteEffects)`

**`app.state.ts`:** added `site: SiteState` to `AppState`

### What I Kept

- Full implementation as proposed
- `device.role ?? device.model` fallback — graceful degradation if backend doesn't return `role`
- `selectOnlineCount` / `selectOfflineCount` as derived selectors from the devices array rather than using `site.onlineDeviceCount` — more accurate since these are computed from the actual device list loaded in the store
- Site route nested inside `ORGANIZATION_ROUTES` (not a top-level app route) — keeps `orgId` available in the same URL segment

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Device name links point to `/organizations/:orgId/sites/:siteId/devices/:deviceId` which does not yet exist — will resolve to 404 until the Device detail page is implemented

### Reusable?

`yes` — same NgRx feature store + `toSignal()` + three-file component pattern used for Dashboard, Organization, and Site pages; the Device page can follow identically.

---

## Entry 18 — Edge Device Detail Page

### Date

`2026-05-31`

### Goal

Implement the full Edge Device Detail page with header info, 6 compact info tiles, a Port Status card (WAN/LAN interface tables), and an Uplink Bandwidth card with a Chart.js time-series line chart and time-range selector.

### Exact Prompt

```text
Implement the edge page using angular 17 components

Details:
1. header information — Site name, Site description
2. Tile information — Role, Status, Uptime, System IP, Model, Site
3. Port status (Live) — card with WAN Interfaces table and LAN Interfaces table
   Columns: Interface, Status (UP/DOWN), IP Address
4. Uplink bandwidth chart
   - Title and description as util card component
   - Time range dropdown (1h, 6h, 24h)
   - Line chart of WANs (WAN1, WAN2) time series on x-axis
   - WAN stats (current, average, peak Mbps)

Refer the mock up in public/frontend/final-edge.png
```

### Files / Context Provided

- `frontend/public/final_dashboard_mock/final-edge.png` (reference mockup)
- `frontend/src/app/core/models/device.model.ts`
- `frontend/src/app/core/sdwan-api.service.ts`
- `frontend/src/app/shared/components/tile/tile.component.ts`
- `frontend/src/app/features/organizations/organizations.routes.ts`
- `frontend/src/app/store/app.state.ts`

### Output Summary

AI created 10 new files and updated 5 existing files:

**Model updates:**
- `device.model.ts` — added optional `role?` and `uptime?` to `DeviceDetail`
- `tile.component.ts/scss` — added `compact = input(false)`; when true, count renders at `1.05rem` instead of `2rem`

**New shared `CardComponent` (3 files):**
- Reusable card wrapper with `title` (required) + `description` (optional) inputs
- Named `[card-actions]` ng-content slot in the header for the time-range dropdown
- Body `ng-content` slot for arbitrary content

**NgRx device store (4 files):**
- `device.actions.ts` — `loadDevice` / `loadWanHistory` action groups; `loadWanHistory` carries the `range` param, which also updates `selectedRange` in the reducer
- `device.reducer.ts` — `DeviceState` with device, wanHistory, selectedRange, dual loading flags
- `device.selectors.ts` — 10 selectors; `selectWanInterfaces` / `selectLanInterfaces` filter by `type === 'WAN'/'LAN'`
- `device.effects.ts` — two functional effects

**`DeviceDetailComponent` (3 files):**
- Breadcrumb: Organization → Site (from a direct `getSite()` call stored in a component signal) → Device
- 6 compact tiles: Role (`device.role ?? '—'`), Status (type=success/danger), Uptime, System IP, Model, Site
- Port Status card: separate WAN and LAN tables with UP/DOWN badges
- Uplink Bandwidth card: Chart.js `line` type registered in this file; `effect()` + `ngAfterViewInit` guard for build/update; `wanStats` is a `computed()` deriving current/average/peak from `wanHistory` signal
- Time range `<select>` dispatches `loadWanHistory` with new range; `selectedRange` reflects store state

**Routing and state:**
- `':orgId/sites/:siteId/devices/:deviceId'` added to `ORGANIZATION_ROUTES`
- `DeviceState` added to `AppState`

### What I Kept

- Full implementation as proposed
- `effect()` + `ngAfterViewInit` chart build pattern — handles async data arrival before or after view init
- `computed()` for `wanStats` — reactive, zero extra subscriptions
- `[card-actions]` named slot on `CardComponent` — clean separation of header actions from body content
- `compact` input on `TileComponent` instead of a new component — extends existing pattern minimally

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- `NetworkInterface` was missing `ipAddress` field — both interface tables showed `iface.name` as a placeholder; fixed in follow-up by adding `ipAddress?: string` to the model and updating the template to `iface.ipAddress ?? '—'`
- `(tileClick)="null"` on the Site tile was invalid Angular binding — fixed by adding `navigateToSite()` method using `Router.navigate()`
- `interfaceStatus()` only matched `'UP'`; backends returning `'ONLINE'` would show `DOWN` — fixed to accept both `'UP'` and `'ONLINE'` as up states
- Dead `interfaceIp()` method (always returned `iface.name`) was left in — removed in follow-up

### Follow-up fixes applied (same session)

| # | Bug | Fix |
|---|---|---|
| 1 | `NetworkInterface` missing `ipAddress` | Added `ipAddress?: string` to model; template shows `iface.ipAddress ?? '—'` |
| 2 | `(tileClick)="null"` invalid binding | Replaced with `(tileClick)="navigateToSite()"` using injected `Router` |
| 3 | `interfaceStatus()` only matched `'UP'` | Updated to accept `'UP'` or `'ONLINE'` → `'UP'`, else `'DOWN'` |
| 4 | Dead `interfaceIp()` method | Removed entirely |

### Reusable?

`yes` — `CardComponent` is reusable across any page needing a titled panel with optional header actions. The Chart.js `effect()` + `ngAfterViewInit` pattern is reusable for any signal-driven chart component.

---

## Entry 19 — Fix Chart.js Line Chart Generic Type Error

### Date

`2026-05-31`

### Goal

Resolve `TS2322` compilation error in `device-detail.component.ts` caused by `toChartData()` returning `object[]` datasets, which prevented TypeScript from narrowing the `Chart<'line'>` constructor's data type.

### Exact Prompt

```text
I have angular compilation issue while running serve

Exception:
TS2322: Type 'Chart<"line", DistributiveArray<ChartTypeRegistry[TType]["defaultDataPoint"]>, unknown>'
is not assignable to type 'Chart<"line", (number | Point | null)[], unknown>'.
  ...
    Type 'DistributiveArray<ChartTypeRegistry[TType]["defaultDataPoint"]>' is not assignable to
    type '(number | Point | null)[]'.

Code Location: src/app/features/devices/device-detail.component.ts:156:4

Analyse and fix this issue without changing any business logic.
```

### Files / Context Provided

- `frontend/src/app/features/devices/device-detail.component.ts`

### Output Summary

Root cause: `toChartData()` was typed to return `{ labels: string[]; datasets: object[] }`. The `object[]` type is too wide — TypeScript cannot narrow it to `(number | Point | null)[]`, so the `new Chart<'line'>` constructor infers `DistributiveArray<...>` instead, which is incompatible with the `Chart<'line'>` field declaration.

Two-line fix:
1. Added `type ChartData` to the `chart.js` import
2. Changed `toChartData()` return type from `{ labels: string[]; datasets: object[] }` to `ChartData<'line', number[]>`

No business logic changed — same pattern as the earlier `DonutChartComponent` fix (Entry 9).

### What I Kept

- Fix as proposed — minimal and correct.

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — root cause and fix were identical to the doughnut chart generic issue fixed in Entry 9.

### Reusable?

`yes` — whenever a Chart.js chart field is typed as `Chart<'line'>` or `Chart<'doughnut'>`, both the constructor call and any data-building helper must use the matching typed generic (`ChartData<'line', number[]>`) to avoid `DistributiveArray` inference errors.

---

## Entry 20 — Fix WAN Stats to Use TileComponent

### Date

`2026-05-31`

### Goal

Replace raw `div.wan-stat` HTML blocks in the Uplink Bandwidth card with `TileComponent` instances to match the mockup's card-style WAN stat display.

### Exact Prompt

```text
In Device page, In uplink card WAN details are not rendered as tile card.
Check the mockup (frontend/public/final-edge.png)
```

### Files / Context Provided

- `frontend/public/final_dashboard_mock/final-edge.png` (reference mockup)
- `frontend/src/app/features/devices/device-detail.component.html`
- `frontend/src/app/features/devices/device-detail.component.ts`
- `frontend/src/app/features/devices/device-detail.component.scss`

### Output Summary

AI identified that the WAN stats section below the line chart was implemented as raw `div.wan-stat` HTML instead of `TileComponent` instances. Three files were updated:

- **`device-detail.component.html`** — replaced `div.wan-stat` blocks with `<app-tile [title]="stat.name" [count]="stat.current + ' Mbps'" [subtitle]="'Avg: ... · Peak: ...'" [type]="wanTileType(i)" />` inside a `@for` loop using `$index`
- **`device-detail.component.ts`** — imported `TileType`; added `wanTileType(index)` helper mapping index → `TileType` (`info` for WAN1 = blue, `success` for WAN2 = green, matching chart line colours)
- **`device-detail.component.scss`** — removed dead `.wan-stats`/`.wan-stat` rules; replaced with lean `.wan-tile-row` flex container

### What I Kept

- Full changes as proposed
- `wanTileType()` colour mapping aligned with `LINE_COLORS` array (index 0 = blue/info, index 1 = green/success) — visual consistency between chart lines and tiles

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — the fix correctly identified the gap and used the existing `TileComponent` pattern.

### Reusable?

`yes` — the `$index` + type-mapping pattern for dynamically coloured tile rows is reusable wherever a list of metrics needs visually distinct tiles matching a chart legend.

---

## Entry 21 — Implement DeviceRole Enum End-to-End

### Date

`2026-05-31`

### Goal

Implement `DeviceRole` (ACTIVE / STANDBY) end-to-end: new enum in the Spring Boot backend, propagated through the domain model and DTOs, seeded in `MockDataStore`, and adopted in the Angular frontend models.

### Exact Prompt

```text
In device page, role is a ENUM like ACTIVE | STANDBY not the description.
Check the backend implementation if role is not implemented, then implement it in API
and update the model and adopt in front end.
```

### Files / Context Provided

- `backend/src/main/java/com/example/sdwan/domain/Device.java`
- `backend/src/main/java/com/example/sdwan/dto/DeviceSummaryDto.java`
- `backend/src/main/java/com/example/sdwan/dto/DeviceDetailDto.java`
- `backend/src/main/java/com/example/sdwan/data/MockDataStore.java`
- `backend/src/main/java/com/example/sdwan/service/impl/DeviceServiceImpl.java`
- `backend/src/main/java/com/example/sdwan/service/impl/SiteServiceImpl.java`
- `frontend/src/app/core/models/device.model.ts`
- `frontend/src/app/core/models/site.model.ts`

### Output Summary

AI confirmed via codebase inspection that `role` was completely absent from all backend layers. Implemented across 7 backend files and 2 frontend files:

**Backend:**
- `DeviceRole.java` — new enum: `ACTIVE`, `STANDBY`
- `Device.java` — added `role: DeviceRole` field with constructor, accessor, equals/hashCode/toString
- `DeviceSummaryDto.java` — same addition
- `DeviceDetailDto.java` — same addition
- `MockDataStore.java` — `device()` helper extended with `DeviceRole` param; all 12 devices assigned explicit roles: EDGE-01/CORE → `ACTIVE`, EDGE-02/BACKUP-01 → `STANDBY`
- `SiteServiceImpl.java` — `toDeviceSummary()` passes `d.role()`
- `DeviceServiceImpl.java` — `getDeviceById()` passes `device.role()`

**Frontend:**
- `device.model.ts` — added `DeviceRole = 'ACTIVE' | 'STANDBY'` type; `DeviceDetail.role` updated from `string?` to `DeviceRole?`
- `site.model.ts` — `DeviceSummary.role` updated from `string?` to `'ACTIVE' | 'STANDBY'?`

The device page template already used `d.role ?? '—'` — no template changes needed; the Role tile displays correctly once the backend restarts.

### What I Kept

- Full implementation as proposed
- Explicit role assignment per device in `MockDataStore` rather than a derived rule — ensures determinism and makes mock data intent clear
- `DeviceRole` placed in `domain` package alongside `DeviceStatus` — consistent with existing enum placement

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — full end-to-end implementation was correct on the first pass.

### Reusable?

`yes` — the pattern (new domain enum → domain class field → DTO field → MockDataStore seed → service mapper → frontend type) is the standard checklist for adding any new enum-backed field to this backend.

---

## Entry 22 — Remove Unimplemented Nav Items from Sidebar

### Date

`2026-05-31`

### Goal

Clean up the sidebar navigation to only show routes that are implemented, removing Sites, Edge Devices, Alerts, Reports, and Settings.

### Exact Prompt

```text
Since Sites and Edge Devices are not directly accessible, remove their entries from the
navigation menu.

Also, Alerts, Reports, and Settings are not part of the current scope and are not planned
for implementation at this stage. Remove these menu items as well.
```

### Files / Context Provided

- `frontend/src/app/app.component.html`

### Output Summary

Removed 5 nav links from the sidebar (`Sites`, `Edge Devices`, `Alerts`, `Reports`, `Settings`). The sidebar now contains only `Dashboard` and `Organization` — the two fully implemented routes.

### What I Kept

- Change as proposed — minimal one-location edit.

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing.

### Reusable?

`no` — this is a one-time scoping decision specific to the current project state.

---

## Entry 23 — Remove NgRx State Management

### Date

`2026-05-31`

### Goal

Remove all NgRx state management dependencies and implementation code from the Angular frontend, replacing store/effects/selectors with direct service calls and Angular Signals.

### Exact Prompt

```text
Analyze the application and remove all state management dependencies and related
implementation code.
```

### Files / Context Provided

- `frontend/package.json`
- `frontend/src/app/app.config.ts`
- `frontend/src/app/features/dashboard/dashboard.component.ts`
- `frontend/src/app/features/dashboard/dashboard.routes.ts`
- `frontend/src/app/features/organizations/organization-detail.component.ts`
- `frontend/src/app/features/organizations/organizations.routes.ts`
- `frontend/src/app/features/sites/site-detail.component.ts`
- `frontend/src/app/features/devices/device-detail.component.ts`

### Output Summary

AI audited all NgRx usage and removed it end-to-end across 17 deleted files and 8 updated files:

**Deleted (17 files):**
- All 4 feature `store/` directories (`dashboard`, `organizations`, `sites`, `devices`) — actions, reducers, selectors, effects
- `store/app.state.ts`

**Updated:**
- `package.json` — removed `@ngrx/store`, `@ngrx/effects`, `@ngrx/store-devtools`
- `app.config.ts` — removed `provideStore`, `provideEffects`, `provideStoreDevtools`
- `dashboard.routes.ts` / `organizations.routes.ts` — plain `loadComponent` routes, all `provideState`/`provideEffects` providers removed
- `dashboard.component.ts` — `Store` → `SdwanApiService`; `toSignal(store.select(...))` → `signal()`; dispatch calls → `forkJoin`; NgRx selectors inlined as `computed()`
- `organization-detail.component.ts` — same pattern; `totalDevices` selector → `computed()` from the sites signal
- `site-detail.component.ts` — `Store` → `SdwanApiService`; `devices`/`onlineCount`/`offlineCount` selectors → `computed()` derived from the `site` signal
- `device-detail.component.ts` — `Store` → direct API calls; `onRangeChange` dispatch → `fetchWanHistory()` private helper; `wanInterfaces`/`lanInterfaces` selectors → `computed()`

Post-refactor grep confirmed zero NgRx references remain in the codebase.

### What I Kept

- Full changes as proposed
- `computed()` for all derived state — reactive, no extra subscriptions, replaces NgRx selectors cleanly
- `forkJoin` for parallel API calls (dashboard overview + site health snapshot, org + sites)
- `fetchWanHistory()` private helper in device component — replaces the effect pattern with a direct, readable method

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — all NgRx references were removed cleanly and the replacement signal pattern preserves identical behaviour.

### Reusable?

`yes` — the pattern (`signal()` for state + `computed()` for derived data + direct service calls + `forkJoin` for parallel requests) is a clean, dependency-free alternative to NgRx for any Angular 17 application where full flux architecture is not required.

---

## Entry 24 — Implement Device Uptime End-to-End

### Date

`2026-05-31`

### Goal

Add `uptime` as a String field to the backend device layer — domain class, DTOs, MockDataStore seeding, and service mappers — following the same pattern used for `deviceRole`.

### Exact Prompt

```text
Add Device Uptime Support

Implement device uptime support across the backend. Add a new uptime field to Device.java
(example value: "5d 14h 32m"). Propagate this field through all relevant DTOs, mappers,
service layers, and API responses so it is available end-to-end. Follow the same
implementation pattern currently used for deviceRole to maintain consistency with the
existing codebase. Update MockDataStore to seed realistic uptime values for all devices.
Ensure the application compiles successfully and that the new field is consistently exposed
wherever device information is returned.
```

### Files / Context Provided

- `backend/src/main/java/com/example/sdwan/domain/Device.java`
- `backend/src/main/java/com/example/sdwan/dto/DeviceSummaryDto.java`
- `backend/src/main/java/com/example/sdwan/dto/DeviceDetailDto.java`
- `backend/src/main/java/com/example/sdwan/data/MockDataStore.java`
- `backend/src/main/java/com/example/sdwan/service/impl/SiteServiceImpl.java`
- `backend/src/main/java/com/example/sdwan/service/impl/DeviceServiceImpl.java`

### Output Summary

Added `uptime: String` across 6 backend files following the exact `deviceRole` pattern:

- `Device.java` — field, constructor param (between `role` and `ipAddress`), accessor, equals/hashCode/toString
- `DeviceSummaryDto.java` — same addition, exposed in site-detail device list
- `DeviceDetailDto.java` — same addition, exposed in device-detail API
- `MockDataStore.java` — `device()` helper extended with `uptime` param; 12 devices seeded with realistic values (online devices: e.g. `"5d 14h 32m"`, `"22d 11h 30m"`; offline devices: `"—"`)
- `SiteServiceImpl.toDeviceSummary()` — passes `d.uptime()`
- `DeviceServiceImpl.getDeviceById()` — passes `device.uptime()`

Frontend `DeviceDetail.uptime?` was already typed as `string?`; the Uptime tile template uses `d.uptime ?? '—'` and will display correctly once the backend restarts.

### What I Kept

- Full implementation as proposed
- `"—"` for offline devices — semantically correct (uptime is undefined when device is down)
- Field position consistent with `role` ordering in constructor and equals/hashCode

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — identical pattern to `deviceRole` applied cleanly.

### Reusable?

`yes` — the checklist (domain field → DTO field → MockDataStore seed + helper param → service mapper) is the standard pattern for adding any new plain-type field to this backend.

---

## Entry 25 — Add Network Interface IP Address End-to-End

### Date

`2026-05-31`

### Goal

Add `ipAddress` to `NetworkInterface.java` and `InterfaceDto.java`, propagate through the mapper and API response, and seed realistic IP values for all 20 interfaces in `MockDataStore`.

### Exact Prompt

```text
Add Network Interface IP Address Support

Implement IP address support for network interfaces. Add a new ipAddress field to both
NetworkInterface.java and InterfaceDto.java, and update all related mappers, services,
and API response models to propagate the field end-to-end. Seed realistic IP addresses
in MockDataStore, using values similar to those shown in the UI mockups, such as
100.68.1.1, 192.168.x.x, and other valid private/internal network IP ranges. Follow
existing project conventions and patterns, keep API responses consistent, and verify
that the project builds and runs successfully after the changes.
```

### Files / Context Provided

- `backend/src/main/java/com/example/sdwan/domain/NetworkInterface.java`
- `backend/src/main/java/com/example/sdwan/dto/InterfaceDto.java`
- `backend/src/main/java/com/example/sdwan/data/MockDataStore.java` (seedInterfaces + helpers)
- `backend/src/main/java/com/example/sdwan/service/impl/DeviceServiceImpl.java` (toInterfaceDto)

### Output Summary

Added `ipAddress: String` across 4 backend files following the same pattern as `uptime` and `role`:

- `NetworkInterface.java` — field added between `status` and `speedMbps`; constructor, accessor, equals/hashCode/toString updated
- `InterfaceDto.java` — field added after `status`; same structural changes
- `MockDataStore.java` — `wanIface()` and `lanIface()` helpers updated with `ipAddress` param; all 20 interface calls seeded:
  - WAN: `100.68.{site}.{n}` (e.g. `100.68.1.1`, matching mockup hint)
  - LAN: `192.168.{site}.{n}/24` (e.g. `192.168.1.1/24`)
- `DeviceServiceImpl.toInterfaceDto()` — passes `iface.ipAddress()`

Frontend `NetworkInterface.ipAddress?` was already typed and the Port Status table uses `iface.ipAddress ?? '—'`; will display correctly once backend restarts.

### What I Kept

- Full implementation as proposed
- IP scheme: `100.68.x.x` for WAN (matches the mockup value of `100.68.1.1`), `192.168.x.x/24` CIDR notation for LAN
- Offline interfaces retain IP addresses — the interface is configured even when down

### What I Changed Manually

- None

### What AI Missed Or Got Wrong

- Nothing — same pattern as `uptime` applied cleanly.

### Reusable?

`yes` — same 4-file checklist (domain → DTO → MockDataStore helper + seed calls → service mapper) applies to any new field on `NetworkInterface`.

---

## Entry 26 — Apply Box-Shadow to TileComponent

### Date

`2026-05-31`

### Goal

Replace the raw `grey` named-colour shadow on `TileComponent` with a polished `rgba` shadow that scales through resting → hover → active states, and restore the commented-out border-left type accent colours.

### Exact Prompt

```text
apply box-shadow in tile component
```

### Files / Context Provided

- `frontend/src/app/shared/components/tile/tile.component.scss`

### Output Summary

Three changes to `tile.component.scss`:

| What | Before | After |
|---|---|---|
| Resting shadow | `2px 2px 2px 0px grey` | `0 2px 8px rgba(0,0,0,.08)` — soft, diffused |
| Hover shadow (clickable) | `0 4px 14px rgba(0,0,0,.1)` | `0 6px 16px rgba(0,0,0,.12)` — consistent scale step |
| Border-left accents | Commented out | Restored — all 5 type variants active |

The shadow follows a three-step scale: resting → hover (lifted) → active (pressed), matching the motion convention used across the rest of the dashboard panels.

### What I Kept

- Fix as proposed.

### What I Changed Manually

- Border-left accent lines were re-commented after the fix — the type accent visibility depends on whether the left-border design is preferred.

### What AI Missed Or Got Wrong

- Nothing — shadow values and accent restoration were correct.

### Reusable?

`yes` — the three-step `rgba` shadow scale (resting / hover / active) is a reusable pattern for any interactive card component.

---

## Entry 27 — Rename Mock Sites to Match Reference Mockup (Indian Cities)

### Date

`2026-06-01`

### Goal

Align the backend mock data with the reference dashboard mockups by renaming the seeded sites/devices to the Indian-city naming shown in the mockup screenshots, and adjust the site/device count and health distribution to match exactly what the mockup displays.

### Exact Prompt

```text
Convert these site names as per the mockup with Indian sites.
The reference screenshots (final-overview.png, final-organization.png,
final-site.png, final-edge.png) show Indian-city branches, but the
backend mock data currently uses US cities. Update MockDataStore so the
seeded organisation, sites, devices, and interfaces match the mockup:
5 sites (Mumbai-Branch, Bangalore-Branch, Pune-Branch, Chennai-Branch,
Hyderabad-DC) with the health distribution shown
(3 HEALTHY, 1 DEGRADED, 1 DOWN).
```

### Files / Context Provided

- `backend/src/main/java/com/example/sdwan/data/MockDataStore.java`
- `frontend/public/final_dashboard_mock/final-overview.png` (reference mockup)
- `frontend/public/final_dashboard_mock/final-organization.png` (reference mockup)
- `frontend/public/final_dashboard_mock/final-site.png` (reference mockup)

### Output Summary

Rewrote the seed data in `MockDataStore` to match the mockup:
- Organisation renamed `Acme Networks` (North America) -> `Acme Corporation` (India)
- Sites changed from 4 US cities (New York HQ, Los Angeles Branch, Chicago Office, Seattle Data Center) to 5 Indian sites: `Mumbai-Branch`, `Bangalore-Branch`, `Pune-Branch`, `Chennai-Branch`, `Hyderabad-DC`
- Devices reduced from 12 to 9 and renamed to city-prefixed edge devices (`MUM-EDGE-01/02`, `BLR-EDGE-01/02`, `PUN-EDGE-01/02`, `CHN-EDGE-01/02`, `HYD-EDGE-01`); models switched to `vEdge Cloud` / `vEdge 100` / `vEdge 5000`
- Health distribution set to match the overview screenshot: Mumbai HEALTHY, Bangalore HEALTHY, Pune DEGRADED (1 device offline), Chennai DOWN (both offline), Hyderabad HEALTHY -> totals 3 HEALTHY / 1 DEGRADED / 1 DOWN, 6 online / 3 offline devices
- Interfaces renamed from `GigabitEthernet0/0/0` style to the mockup's `WAN1` / `WAN2` / `LAN1` / `LAN2` / `LAN3` labels; IP scheme realigned per site (`100.64.x` Mumbai, `100.65.x` Bangalore, etc.)
- Sine-wave WAN traffic parameters re-tuned for all 9 devices, with offline devices' interfaces set to zero traffic

### What I Kept

- The deterministic sine-wave WAN-history generator (unchanged formula) — only the per-interface base/amplitude parameters were re-mapped to the new device IDs
- The `@PostConstruct` single-source-of-truth seeding approach
- Site health still computed from device statuses (not hardcoded) — only the seed data changed, the aggregation logic was untouched

### What I Changed Manually

- (fill in anything you adjusted after the rewrite — e.g. tweaked a specific IP, uptime string, or traffic value — otherwise leave as "None")

### What AI Missed Or Got Wrong

- The first attempt to edit the device block failed on an em-dash (—) encoding mismatch in the offline-device uptime values; the file had to be rewritten in full rather than patched in place
- Site-name alignment is driven entirely by the mockup screenshots, not by `instruction.md` (which does not specify city names) — this is a presentation-matching decision, not a hard requirement

### Reusable?

`no` — this is project-specific seed data tuned to one set of mockups. The approach (read the mockup, align deterministic seed data to it) is reusable, but the specific names/values are not.

---

## Example Entry

### Date

`2026-04-22`

### Goal

Define the first backend and frontend slices from the minimal starter.

### Exact Prompt

```text
I have a minimal Angular + Spring Boot starter for an SD-WAN dashboard assignment.
Help me define:
- backend contracts for organization, site, and device views
- the first Angular routes/screens to implement
- the smallest useful validation steps after each slice

Use deterministic mock data and include the site health aggregation rules.
```

### Files / Context Provided

- `boilerplate/backend/src/main/java/com/example/sdwan/api/SdwanController.java`
- `boilerplate/frontend/src/app/app.component.ts`
- `boilerplate/instruction.md`

### Output Summary

The AI proposed initial endpoint contracts, a sensible screen order, and validation checkpoints for building on top of the starter.

### What I Kept

- Clear endpoint separation
- Incremental implementation order
- Deterministic mock dataset idea

### What I Changed Manually

- Tightened naming
- Reduced overbuilt fields
- Verified the steps against the actual starter structure

### What AI Missed Or Got Wrong

- Needed clearer error-state handling
- Needed a more explicit WAN-only device requirement

### Reusable?

`yes` because it anchors the model to the starter state instead of assuming the dashboard already exists.
