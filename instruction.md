# SD-WAN Dashboard Assignment Instructions

This starter is intentionally smaller than before. It only gives you a runnable Angular app, a runnable Spring Boot app, and a tiny `GET /api/health` handshake. The actual dashboard, routing, mock data contracts, site aggregation logic, and WAN charting are now part of the assignment.

## Goal

Build a small SD-WAN monitoring dashboard with a Java backend and an Angular frontend.

The finished experience should let a user move through:

1. Organization view
2. Site view
3. Device view

The device view should include WAN-focused telemetry. The assignment should feel like a thoughtful monitoring tool, not just a set of disconnected screens.

## What The Starter Provides

- A Spring Boot app that runs on `http://localhost:8080`
- A single starter endpoint at `GET /api/health`
- An Angular app that runs and verifies backend reachability
- A prompt log template at [prompts/prompt-log.md](/boilerplate/prompts/prompt-log.md)
- Optional large mock datasets at `mock-data/chart-api-datasets.json`

## What You Need To Build

### Frontend

- A dashboard or organization-level landing view
- Organization -> Site -> Device navigation
- Clear summaries for site and device health
- A device page with WAN-only bandwidth history
- Loading and error handling that shows deliberate engineering

### Backend

- In-memory mock data for organization, sites, devices, and WAN history
- Endpoint contracts that support the frontend views you build
- Deterministic sample data that is easy to reason about
- Sensible 404 or validation behavior for missing resources

### Domain Rules

Use these site health rules:

- `HEALTHY`: all devices online
- `DEGRADED`: at least one device offline
- `DOWN`: all devices offline

Keep the device telemetry WAN-centric. LAN interface data can exist, but the assignment should clearly emphasize WAN monitoring.

## Recommended Scope

A strong submission usually includes:

- A dashboard summary screen
- A site list or organization screen
- A site detail screen with device summaries
- A device detail screen with status, interface context, and WAN history
- A small but coherent mock API that serves those views

It does not need production-scale auth, persistence, or full enterprise polish.

## What To Run

You will usually run two processes:

1. Java backend
2. Angular frontend

Optional:

3. Large dataset generator for chart stress testing

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 20+
- npm 10+

## How To Run

### Backend

From the repository root:

```bash
cd boilerplate/backend
mvn -Dmaven.repo.local=/tmp/sdwan-m2 spring-boot:run
```

Starter smoke test:

```bash
curl -s http://localhost:8080/api/health
```

### Frontend

From the repository root:

```bash
cd boilerplate/frontend
npm install
npm start
```

Production build:

```bash
cd boilerplate/frontend
npm run build
```

### Optional Large Dataset Generator

```bash
cd boilerplate
node tools/generate-large-chart-data.mjs
```

Generated file:

- `boilerplate/mock-data/chart-api-datasets.json`

You do not need to use the large datasets unless you want to discuss or demonstrate chart performance tradeoffs.

## Suggested Build Order

1. Define the backend response shapes you actually want to support.
2. Create deterministic in-memory mock data.
3. Build organization and site summaries first.
4. Add site detail and device detail flows.
5. Add WAN history and time-range controls if you want them.
6. Tighten loading states, empty states, and error handling.
7. Validate with a frontend build and a backend smoke test.

## AI Usage Expectations

Use AI as an assistant, not as a substitute for engineering judgment.

Good use of AI for this assignment looks like:

- breaking the work into steps
- drafting DTOs or UI skeletons
- debugging with exact error output
- comparing implementation options
- generating test ideas or edge cases

Weak use of AI looks like:

- pasting a generic full solution without understanding it
- accepting output without validation
- failing to record what AI helped with

## Prompt Patterns

These prompts fit the assignment better now that less code is prebuilt.

### Planning Prompt

```text
You are helping with an SD-WAN dashboard assignment.
I have a minimal Angular + Spring Boot starter only.

I need to build:
- organization/site/device flow
- mock backend contracts
- site health aggregation
- WAN-only telemetry views

Break this into small implementation steps with validation points.
```

### Backend Prompt

```text
Create Spring Boot DTOs and mock endpoint contracts for an SD-WAN dashboard.

Required views:
- organization summary
- site detail
- device detail
- WAN history for the device page

Rules:
- HEALTHY = all devices online
- DEGRADED = at least one device offline
- DOWN = all devices offline

Prefer deterministic in-memory data and simple endpoint contracts.
```

### Frontend Prompt

```text
Create Angular screens for an SD-WAN dashboard from a minimal starter.

Required flow:
- dashboard or organization landing page
- site detail page
- device detail page

Requirements:
- clear navigation between levels
- WAN-only chart or WAN history view
- no unnecessary dependencies
- readable code with sensible loading and error states
```

### Debugging Prompt

```text
I have a Spring Boot / Angular issue.
Here is the exact error:
<paste exact error>

Here is the relevant code:
<paste exact code>

Explain root cause first, then propose the smallest safe fix.
```

### Performance Prompt

```text
I need to discuss performance for an SD-WAN dashboard with large WAN time-series datasets.
Input data may reach 100k to 200k points.

Suggest:
1. the biggest wins first
2. rendering strategy
3. API payload strategy
4. Angular state and change-detection considerations
5. tradeoffs
```

## Prompt Documentation

Document meaningful AI interactions in:

- [prompts/prompt-log.md](/Users/nirmroy/Documents/AI_Take_Home/boilerplate/prompts/prompt-log.md)

For each prompt you keep or learn from, capture:

- date
- goal
- exact prompt
- files or context provided
- what you kept
- what you changed manually
- what AI missed or got wrong
- whether the prompt is reusable

This matters because interviewers usually care less about whether AI was used and more about whether the candidate stayed in control.

## Performance Discussion Checklist

If you decide to address large datasets, these are the right tradeoffs to think about.

### Frontend Rendering

- Avoid drawing every point when only a limited number of pixels are visible.
- Consider downsampling or aggregated resolutions for larger time windows.
- Prefer rendering approaches that stay responsive with larger series.
- Keep heavy preprocessing off the hot render path.

### Angular State

- Avoid repeated refetches for unchanged route/time-range combinations.
- Be deliberate about where derived chart data is computed.
- Use a state approach that keeps updates predictable.

### API Design

- Return only data needed for the current screen.
- Consider multiple resolutions such as `raw`, `1m`, `5m`, `15m`, `1h`.
- Keep payloads compact if you discuss very large ranges.
- Use numeric timestamps if payload size becomes important.

### UX

- Show high-value summary information before expensive chart rendering.
- Make loading states explicit.
- Let users narrow time ranges rather than always defaulting to the largest window.

## Submission Checklist

Before you consider the assignment done, make sure you can show:

- frontend starts successfully
- backend starts successfully
- the main UI flow works
- data contracts are coherent and easy to explain
- site health aggregation matches the rules above
- prompts are documented
- you can explain tradeoffs and what you would improve next
