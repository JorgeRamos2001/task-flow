# ADR 0001: Monorepo and modular monolith

- Status: Accepted
- Date: 2026-10-08

## Context

TaskFlow is a new project with one Spring Boot backend scaffold, no Angular client yet, a shared UI design, and coordinated API/UI feature work.

## Decision

Keep frontend, backend, infrastructure, and product/API contracts in one Git repository. Run them as separately built/deployed applications. Implement the backend as a modular monolith with explicit `auth`, `users`, `boards`, `tasks`, `notifications`, and `realtime` boundaries.

## Consequences

- A cross-stack feature can be reviewed in one pull request while keeping Maven and npm toolchains independent.
- CI can run path-filtered frontend/backend checks.
- API contracts remain the shared boundary; Java and TypeScript domain classes are not shared as source.
- Revisit separate repositories or services only if team ownership, access control, deployment cadence, or scale creates a concrete need.
