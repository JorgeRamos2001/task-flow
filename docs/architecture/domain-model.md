# Domain model draft

The board is the collaboration and authorization boundary. There is no Workspace aggregate in V1.

```text
User ──< BoardMember >── Board ──< BoardColumn ──< Task ──< SubTask
  │                          │                         ├──< Comment
  │                          ├──< BoardInvitation       └──< TaskTag >── BoardTag
  │                          ├──< BoardActivity
  │                          └──< Notification
  ├──< OAuthIdentity
  ├──< RefreshSession
  └──< PasswordResetToken
```

## Entities and constraints

- **User:** profile identity and normalized unique email/username. A local password hash may be absent for a Google-only account.
- **OAuthIdentity:** provider and provider subject are unique; Google identities map to a TaskFlow user.
- **Board:** title, description, fixed-palette theme color, `PERSONAL`/`COLLABORATIVE` mode, original owner, timestamps, and optimistic-lock version.
- **BoardMember:** unique `(board_id, user_id)` with role `OWNER`, `ADMIN`, or `MEMBER`. Enforce at most one Owner with a PostgreSQL partial unique index; create the Owner membership in the same transaction as the board.
- **BoardColumn:** belongs to one board and has a stable order position.
- **Task:** belongs to one column/board, has one optional assignee, title, description, optional UTC due instant, order position, tags, and version. The assignee must be a member of the same board.
- **SubTask:** belongs to one task and stores title, order, completion state, and completion timestamp. Progress is derived from completed subtasks.
- **Comment:** belongs to one task and has one author. Cross-task comments are not supported.
- **BoardTag / TaskTag:** tags are scoped to a board and reusable on its tasks. Owner/Admin maintain definitions; an assigned Member may apply or remove existing board tags on their task.
- **BoardInvitation:** targets an existing user and board, records inviter, assigned non-Owner role, token hash, status, and expiry. Membership is created only upon acceptance.
- **BoardActivity:** append-oriented board event history for the Logs view.
- **Notification:** persisted in-app notification, including read state, recipient, board/task references, and deduplication key.
- **RefreshSession / revoked access token:** supports refresh rotation, logout revocation, and token expiry cleanup; this exists even though V1 has no sessions-management UI.
- **PasswordResetToken:** one-use token hash, user, creation/expiry/consumption timestamps.

## Persistence rules

- Flyway migrations are the schema source of truth; Hibernate validates rather than mutates the schema.
- Keep JPA relationships lazy by default and expose response DTOs, not entities.
- Add indexes for board membership lookups, ordered columns/tasks, task due times, pending invitations, reset-token lookup, and unread notifications.
- Use transactions for board creation/Owner membership, invitation acceptance/membership creation, role changes, and task moves.
- Use forward-only Flyway migrations. Verify them against PostgreSQL through Testcontainers.

## Open date decision

The design shows dates without times and V1 has no timezone preference. Before building due-date input and the scheduler, decide whether a due date means an exact instant or the end of the selected day in the browser's timezone. Store the resolved instant as UTC either way.
