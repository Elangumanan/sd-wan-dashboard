# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an SD-WAN dashboard assignment — a monorepo with a Spring Boot mock API backend and an Angular frontend. The task is to build a 3-level navigation dashboard: Organization → Site → Device, with WAN telemetry on device pages.

Reference screenshots showing the target UI are in `frontend/public/final_dashboard_mock/`.

## Commands

### Frontend (`frontend/` directory)
```
npm install          # install dependencies
npm start            # dev server at http://localhost:4200
npm run build        # production build
npm test             # run unit tests
```

### Backend (`backend/` directory)
```
mvn spring-boot:run                                          # dev server at http://localhost:8080
mvn -Dmaven.repo.local=/tmp/sdwan-m2 spring-boot:run        # alternative with local Maven cache
curl http://localhost:8080/api/health                        # smoke test
```

## Architecture

### Backend — `backend/`
- Spring Boot 3.3.5, Java 17, Maven
- Single package: `com.example.sdwan.api`
- Main class: `SdwanMockApiApplication`
- Controller: `SdwanController` — exposes REST endpoints under `/api/*`, CORS open to all origins
- Currently has only `GET /api/health`; all domain endpoints (sites, devices, telemetry) must be added here
- Config: `src/main/resources/application.properties` (port 8080, app name `sdwan-assignment-starter-api`)

### Frontend — `frontend/`
- Angular 17.3.0 with standalone components, strict TypeScript (ES2022)
- Entry: `src/main.ts` → bootstraps `AppComponent` standalone
- Service: `SdwanApiService` — all HTTP calls go through this service to `http://localhost:8080/api`
- Models: `HealthResponse` interface (currently minimal; domain models to be added)
- The current `AppComponent` is a landing/checklist page — replace or extend it to implement routing

### Data model requirements (from `instruction.md`)
- Three entity levels: **Organization** → **Sites** → **Devices**
- **Site health aggregation**:
  - `HEALTHY` — all devices online
  - `DEGRADED` — at least one device offline (but not all)
  - `DOWN` — all devices offline
- Device page must include **WAN bandwidth history** (time-series chart)

### Supporting files
- `instruction.md` — full assignment scope and rules; read before implementing
- `plans.md` — suggested build order
- `prompts/prompt-log.md` — template for documenting AI usage (fill this out as you go)
- `mock-data/chart-api-datasets.json` — 31 MB optional dataset for performance testing
- `tools/generate-large-chart-data.mjs` — Node.js script to regenerate that dataset

## Key Constraints
- Backend data must be **deterministic** (same response on every call) — no random seeds at runtime
- Angular uses **strict mode** — avoid `any`, handle all null/undefined cases
- Frontend communicates with backend only through `SdwanApiService`; don't add raw `HttpClient` calls in components
