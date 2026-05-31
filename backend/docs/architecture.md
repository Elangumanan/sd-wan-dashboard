# Architecture

## Overview

The backend follows a strict **Controller → Service → Repository** layered architecture. Each layer has a single responsibility and depends only on the layer directly below it. No layer reaches across or skips a layer.

```
┌─────────────────────────────────────────────────────────┐
│                     HTTP Client                          │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP Request
┌────────────────────────▼────────────────────────────────┐
│               GlobalExceptionHandler                     │
│           (@RestControllerAdvice — wraps all layers)     │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  Controller Layer                        │
│   OrganizationController  SiteController  DeviceController│
│              DashboardController                         │
│                                                          │
│   • Validates HTTP-level input (@Pattern, @PathVariable) │
│   • Delegates to service                                 │
│   • Wraps result in SuccessResponse<T>                   │
└────────────────────────┬────────────────────────────────┘
                         │ Domain method call
┌────────────────────────▼────────────────────────────────┐
│                   Service Layer                          │
│  OrganizationService  SiteService  DeviceService         │
│              DashboardService                            │
│                                                          │
│   • Owns business logic (health aggregation, hierarchy   │
│     ownership validation, WAN range slicing)             │
│   • Throws via ExceptionFactory                          │
│   • Maps domain objects → DTOs                           │
└────────────────────────┬────────────────────────────────┘
                         │ Data access
┌────────────────────────▼────────────────────────────────┐
│                 Repository Layer                         │
│  OrganizationRepository  SiteRepository  DeviceRepository│
│  NetworkInterfaceRepository  WanHistoryRepository        │
│                                                          │
│   • Thin interface — only query methods, no logic        │
│   • Backed by InMemory* implementations                  │
│   • Query from MockDataStore (injected as dependency)    │
└────────────────────────┬────────────────────────────────┘
                         │ Read-only maps
┌────────────────────────▼────────────────────────────────┐
│                    MockDataStore                         │
│            (@Component, @PostConstruct)                  │
│                                                          │
│   • Single source of truth for all in-memory data        │
│   • Initialized once at startup, never mutated           │
│   • Deterministic — same data on every run               │
└─────────────────────────────────────────────────────────┘
```

---

## Package Structure

```
com.example.sdwan/
│
├── SdwanMockApiApplication.java        Entry point (@SpringBootApplication)
│
├── api/
│   └── SdwanController.java            Legacy health check endpoint
│
├── config/
│   ├── CorsConfig.java                 Global CORS filter (all origins, /api/**)
│   ├── JacksonConfig.java              Field-visibility serialization (no getters needed)
│   └── OpenApiConfig.java              Swagger UI metadata
│
├── controller/
│   ├── OrganizationController.java     GET /api/organizations[/{id}]
│   ├── SiteController.java             GET /api/organizations/{orgId}/sites[/{siteId}]
│   ├── DeviceController.java           GET /api/.../devices/{id}[/wan-history]
│   └── DashboardController.java        GET /api/dashboard/overview|site-health
│
├── service/
│   ├── OrganizationService.java        Interface
│   ├── SiteService.java                Interface
│   ├── DeviceService.java              Interface
│   ├── DashboardService.java           Interface
│   └── impl/
│       ├── OrganizationServiceImpl.java
│       ├── SiteServiceImpl.java
│       ├── DeviceServiceImpl.java
│       └── DashboardServiceImpl.java
│
├── repository/
│   ├── OrganizationRepository.java     Interface
│   ├── SiteRepository.java             Interface (findAll, findByOrgId, findById)
│   ├── DeviceRepository.java           Interface
│   ├── NetworkInterfaceRepository.java Interface
│   ├── WanHistoryRepository.java       Interface
│   └── impl/
│       ├── InMemoryOrganizationRepository.java
│       ├── InMemorySiteRepository.java
│       ├── InMemoryDeviceRepository.java
│       ├── InMemoryNetworkInterfaceRepository.java
│       └── InMemoryWanHistoryRepository.java
│
├── data/
│   └── MockDataStore.java              @PostConstruct data seeding + accessors
│
├── domain/                             Internal representation (never serialized directly)
│   ├── DeviceStatus.java               Enum: ONLINE | OFFLINE
│   ├── SiteHealth.java                 Enum: HEALTHY | DEGRADED | DOWN
│   ├── InterfaceType.java              Enum: WAN | LAN
│   ├── InterfaceStatus.java            Enum: UP | DOWN
│   ├── Organization.java               final class (immutable)
│   ├── Site.java                       final class (immutable)
│   ├── Device.java                     final class (immutable)
│   ├── NetworkInterface.java           final class (immutable)
│   └── WanDataPoint.java               final class (immutable)
│
├── dto/                                API-facing data shapes (serialized to JSON)
│   ├── OrganizationSummaryDto.java
│   ├── SiteListItemDto.java
│   ├── SiteDetailDto.java
│   ├── DeviceSummaryDto.java
│   ├── DeviceDetailDto.java
│   ├── InterfaceDto.java
│   ├── WanDataPointDto.java
│   ├── WanInterfaceSeriesDto.java
│   ├── WanHistoryDto.java
│   ├── DashboardOverviewDto.java
│   ├── SiteStatusSummaryDto.java
│   ├── DeviceStatusSummaryDto.java
│   └── SiteHealthSnapshotDto.java
│
├── response/                           Generic API response envelope
│   ├── SuccessResponse.java            Generic<T> — wraps all 2xx responses
│   ├── ErrorResponse.java              Wraps all error responses
│   └── ErrorCode.java                  Enum — maps code → HTTP status
│
└── exception/
    ├── AppException.java               Abstract base — carries ErrorCode + HttpStatus
    ├── ResourceNotFoundException.java  404
    ├── ValidationException.java        400
    ├── BusinessException.java          422
    ├── UnauthorizedException.java      401
    ├── ExceptionFactory.java           Factory — sole instantiation point
    └── GlobalExceptionHandler.java     @RestControllerAdvice — one handler per exception type
```

---

## SOLID Principles Applied

### Single Responsibility
Each class has one reason to change:
- `MockDataStore` — owns data initialization only
- `SiteServiceImpl` — owns site business logic only
- `GlobalExceptionHandler` — owns HTTP error mapping only
- `ExceptionFactory` — owns exception instantiation only

### Open/Closed
New exception types can be added without modifying `GlobalExceptionHandler`. Any new subclass of `AppException` is automatically handled by the single `handleAppException` method.

### Liskov Substitution
Every `InMemory*Repository` is a complete, substitutable implementation of its repository interface. Swapping to a JPA implementation would require no changes in the service layer.

### Interface Segregation
Repository interfaces are narrow — each exposes only the query methods its consumers actually call (e.g., `WanHistoryRepository` has only `findByInterfaceId`).

### Dependency Inversion
Controllers depend on service *interfaces*, services depend on repository *interfaces*. No layer depends on a concrete implementation class. Spring injects the concrete implementations at runtime.

---

## Request Flow (Example: `GET /api/organizations/org-001/sites/site-001`)

```
1. Request arrives at DispatcherServlet
2. CorsFilter adds CORS headers
3. SiteController.getSite("org-001", "site-001") is invoked
4. SiteServiceImpl.getSiteById("org-001", "site-001"):
   a. Calls OrganizationRepository.findById("org-001") → throws if absent
   b. Calls SiteRepository.findById("site-001") + filters by orgId → throws if absent or wrong org
   c. Calls DeviceRepository.findBySiteId("site-001") → device list
   d. Computes online/offline counts, calls computeHealth()
   e. Builds SiteDetailDto
5. Controller wraps in SuccessResponse.of(siteDetailDto)
6. JacksonConfig serializes private final fields directly (no getters needed)
7. Response: 200 OK, JSON body
```

---

## Jackson Serialization

All domain classes and DTOs are **immutable final classes** with `private final` fields and record-style accessors (`id()` not `getId()`).

Jackson's default getter-based introspection would produce empty `{}` objects. `JacksonConfig` overrides this globally:

```java
builder.visibility(PropertyAccessor.FIELD,     JsonAutoDetect.Visibility.ANY)
       .visibility(PropertyAccessor.GETTER,     JsonAutoDetect.Visibility.NONE)
       .visibility(PropertyAccessor.IS_GETTER,  JsonAutoDetect.Visibility.NONE);
```

This means Jackson reads `private final` fields directly, and accessor method naming (`id()` vs `getId()`) is irrelevant to JSON output.
