# SD-WAN Dashboard Assignment Starter

After unzipping the package, start the backend from `boilerplate/backend` and the frontend from `boilerplate/frontend`, then open the starter app and review the landing page together with `instruction.md`.

## Getting Started

1. Start the backend:

```bash
cd boilerplate/backend
mvn spring-boot:run
```

2. Start the frontend in a separate terminal:

```bash
cd boilerplate/frontend
npm install
npm start
```

3. Open the starter app in the browser and review:

- the landing page
- `instruction.md`

The starter frontend expects the backend at `http://localhost:8080/api`.

## What You Need To Build

The code under `boilerplate/` is intentionally minimal. Your task is to implement the actual assignment there, including:

- the overview dashboard
- organization -> site -> edge/device navigation
- mock backend APIs
- site health aggregation
- WAN-focused telemetry and detail views

Site health rules:

- `HEALTHY`: all devices online
- `DEGRADED`: at least one device offline
- `DOWN`: all devices offline

## What Is Included

- `backend/`: Spring Boot starter with a minimal health endpoint
- `frontend/`: Angular starter app with a landing page and backend connectivity check
- `instruction.md`: assignment scope, runbook, AI guidance, and submission checklist
- `plans.md`: suggested implementation order
- `prompts/prompt-log.md`: template for documenting meaningful AI usage
- `mock-data/chart-api-datasets.json`: optional large datasets for chart stress testing

## Reference Screenshots

To clarify the expected scope and depth, the starter includes screenshots from the completed implementation:

- Latest reference set: `frontend/public/final_dashboard_mock/`
- Older reference captures: `frontend/public/reference/`

The `final_dashboard_mock` folder includes screenshots for:

- overview
- organization
- site
- edge/device

These screenshots are only visual references for scope, hierarchy, and information density. They are not starter solution code and should not be treated as prebuilt implementation.

## Submission Expectations

Before submitting:

- keep all implementation inside `boilerplate/`
- make sure both frontend and backend run successfully
- document any meaningful AI assistance in `prompts/prompt-log.md`
