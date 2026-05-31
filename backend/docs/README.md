# SD-WAN Dashboard — Backend Documentation

Spring Boot 3 mock REST API for the SD-WAN monitoring dashboard assignment.

---

## Quick Start

```bash
# From the backend/ directory
mvn spring-boot:run

# Smoke test
curl http://localhost:8080/api/health

# Interactive API explorer
open http://localhost:8080/swagger-ui.html
```

---

## Documentation Index

| Document | What it covers |
|---|---|
| [architecture.md](architecture.md) | Package structure, layered architecture, dependency flow, SOLID principles |
| [api-reference.md](api-reference.md) | All endpoints, request/response shapes, status codes, example payloads |
| [data-model.md](data-model.md) | Domain model, DTOs, mock data catalogue, WAN telemetry generation |
| [exception-handling.md](exception-handling.md) | Exception hierarchy, factory pattern, global handler, error response format |
| [design-decisions.md](design-decisions.md) | Key design choices, trade-offs, and what would change in production |

---

## Technology Stack

| Concern | Choice |
|---|---|
| Runtime | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Build tool | Maven 3.9+ |
| Validation | `spring-boot-starter-validation` (Jakarta Bean Validation) |
| API docs | springdoc-openapi-starter-webmvc-ui 2.6.0 (Swagger UI) |
| Testing | JUnit 5, Mockito, Spring MockMvc |
| Persistence | In-memory (no database — deterministic mock data) |

---

## Project Coordinates

```xml
<groupId>com.example</groupId>
<artifactId>sdwan-assignment-starter-api</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

Base URL: `http://localhost:8080`  
API prefix: `/api`
