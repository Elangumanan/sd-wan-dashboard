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
