# Backend package structure

TaskFlow is a modular monolith organized by business capability first and implementation responsibility inside each capability. It is not split into separate Maven modules or microservices in V1.

```text
com.taskflow
├── auth
│   ├── controller
│   ├── dto
│   ├── service
│   ├── repository
│   └── entity
├── users
│   ├── controller
│   ├── dto
│   ├── service
│   ├── repository
│   └── entity
├── boards
│   ├── controller
│   ├── dto
│   ├── service
│   ├── repository
│   └── entity
├── tasks
│   ├── controller
│   ├── dto
│   ├── service
│   ├── repository
│   └── entity
├── notifications
│   ├── controller
│   ├── dto
│   ├── service
│   ├── repository
│   └── entity
├── activity
│   ├── dto
│   ├── service
│   ├── repository
│   └── entity
└── shared
    └── persistence
```

## Responsibilities

- `controller`: HTTP/WebSocket adapters. Controllers validate transport input and delegate; they do not implement domain rules.
- `dto`: request and response types. JPA entities are never serialized directly to the browser.
- `service`: transactional use cases and permission-policy invocation.
- `repository`: Spring Data interfaces and persistence queries for the owning capability.
- `entity`: JPA entities and enums owned by that capability.
- `shared`: small, genuinely cross-cutting infrastructure only. Do not move board/task business rules into `shared`.

`User` and OAuth identities belong to `users.entity`; login, Google callback, JWT, refresh/logout, and password-reset orchestration belong to `auth`. Board membership and the three board roles belong to `boards`. Task, subtask, comment, and task-tag associations belong to `tasks`; board tag definitions belong to `boards`.

Keep module dependencies directional where possible: tasks may reference board/user identities for persistence, while board entities do not depend on task persistence classes. Cross-module REST responses use DTOs. Repositories and controllers will be added with their first vertical slices rather than as empty packages.
