# SD-WAN Dashboard Starter Plan

## Starter Scope

- Keep the shipped code minimal and runnable.
- Do not prebuild the dashboard screens, contracts, or telemetry views.
- Leave the main product decisions inside the assignment itself.

## Suggested Candidate Build Order

1. Define the API shapes needed by the UI.
2. Add deterministic in-memory mock data.
3. Implement organization and site summaries.
4. Implement site detail and device detail routes.
5. Add WAN-only device history and any range controls.
6. Tighten error handling, empty states, and performance notes.

## Assignment Constraints

- Keep the solution readable and intentionally simple.
- Preserve clear Organization -> Site -> Device navigation.
- Compute site health from device status using the stated rules.
- Keep the device telemetry view WAN-centric.
