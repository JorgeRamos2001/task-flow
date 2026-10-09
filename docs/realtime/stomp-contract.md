# STOMP event contract

## Connection and authorization

- WebSocket/SockJS endpoint: `/ws`.
- The browser sends `Authorization: Bearer <access-jwt>` in the STOMP `CONNECT` headers. Do not put tokens in URL parameters.
- Authenticate `CONNECT` and derive the message principal from the validated token.
- Authorize `SUBSCRIBE` to `/topic/board/{boardId}` by checking current board membership. Re-check membership for role changes/removals and revoke access for removed members.
- V1 mutations use REST. Clients do not publish board mutations directly through STOMP.

## Destinations

- `/topic/board/{boardId}` — changes visible to current board members.
- `/user/queue/notifications` — private persisted-notification updates for the authenticated user.

## Event envelope

```json
{
  "eventId": "uuid",
  "type": "TASK_MOVED",
  "boardId": "uuid",
  "aggregateType": "TASK",
  "aggregateId": "uuid",
  "actor": { "id": "uuid", "displayName": "Example User" },
  "occurredAt": "2026-10-08T12:00:00Z",
  "aggregateVersion": 4,
  "payload": {}
}
```

The server sets actor, timestamp, identifiers, and version; clients cannot spoof them. Payloads contain only fields authorized board members can read.

## Initial event types

- Board/column: `BOARD_UPDATED`, `COLUMN_CREATED`, `COLUMN_UPDATED`, `COLUMN_DELETED`, `COLUMN_REORDERED`.
- Task: `TASK_CREATED`, `TASK_UPDATED`, `TASK_MOVED`, `TASK_DELETED`, `TASK_ASSIGNEE_CHANGED`.
- Subtask/comment: `SUBTASK_CREATED`, `SUBTASK_UPDATED`, `SUBTASK_DELETED`, `COMMENT_CREATED`.
- Membership: `INVITATION_CREATED`, `MEMBER_JOINED`, `MEMBER_ROLE_CHANGED`, `MEMBER_REMOVED`.
- Deadline: `TASK_OVERDUE`.

Publish only after the corresponding database transaction commits. Angular applies an event once by `eventId`/`aggregateVersion`; on reconnect or a version gap, reload the board through REST. The in-memory simple broker is sufficient for local/single-instance operation; a multi-instance deployment needs a broker relay or equivalent fan-out.
