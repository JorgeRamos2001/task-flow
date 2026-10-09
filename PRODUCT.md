# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

Angular frontend; Java 25 and Spring Boot 4.1.1 backend with Spring Security; PostgreSQL with JPA/Hibernate; REST and STOMP over SockJS for real-time updates. The Angular client uses `@stomp/stompjs`.

## Users

Individuals and small teams who need to organize work across personal and collaborative boards. Board participants include Owners, Admins, and Members with different board-scoped permissions.

## Product Purpose

TaskFlow helps individuals and small teams organize tasks on boards, collaborate through comments and subtasks, track due dates, and see collaborative board changes in real time. V1 should support complete task workflows with server-enforced access control.

## Positioning

TaskFlow is a Trello-like board and task-management application. No distinct market-positioning claim has been decided; do not invent customer, scale, or performance claims.

## Operating Context

Users work with personal or collaborative boards. A task belongs to one board and is not shared across boards. Board views include Kanban, List, and Timeline; the calendar is a task-due-date view, not a meeting or event calendar. V1 is English-only and must support desktop, tablet, and mobile web layouts.

## Capabilities and Constraints

- Traditional registration and login use name, username, email, and a BCrypt-hashed password. Google OAuth2 is also supported.
- Access JWTs last 15 minutes. Refresh tokens last 7 days and are persisted in a revocable form. Logout revokes the refresh token and blocks the access token until expiry.
- Password recovery by email is in V1. Password-reset links are single-use and short-lived.
- Board roles are exactly Owner, Admin, and Member. There is one Owner per board. Admins can manage non-Owner members. Members can see board tasks and comment on any visible task; they can manage their assigned task and its subtasks but cannot create or delete top-level tasks.
- Board invitations are sent only to registered users by email and remain pending until accepted or declined. Unknown email addresses do not produce invitations.
- Tasks have at most one assignee. The assignee is controlled by Owner/Admin; Members can manage the content and subtasks of their assigned task.
- Task changes on collaborative boards publish authorized real-time events. A scheduled process detects overdue tasks.
- V1 has no Workspace entity, separate calendar-event entity, active-session management UI, 2FA, appearance preferences, notification preferences, language setting, or timezone setting.
- Settings are limited to Profile and Account; Account contains password change and account deletion. Account deletion is blocked while owned collaborative boards remain unresolved.
- Exact account-deletion resolution policy, due-date timezone interpretation, invitation expiry, and comment edit/delete permissions remain architecture decisions to close before implementation.

## Brand Commitments

The product name is TaskFlow. `docs/task-flow.pen` is the user-provided UI reference. Its visual layouts and components inform implementation; any mock permission copy that conflicts with the rules above is not authoritative.

## Evidence on Hand

The repository contains the TaskFlow UI design in `docs/task-flow.pen` and an initial Spring Boot project under `task-flow-backend/`. Design names, dates, task counts, and social-proof copy are illustrative mock content, not verified product evidence.

## Product Principles

- A board is the collaboration and authorization boundary.
- The backend is authoritative for all permission checks.
- Preserve task ownership by board; global views aggregate tasks without moving them between boards.
- Deliver complete, tested vertical slices through the API and Angular client.
- Real-time messages reflect committed server state.
