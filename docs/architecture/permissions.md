# Board permission contract

Authorization is evaluated on the server for every REST operation and every WebSocket subscription. Hiding a control in Angular is not an authorization mechanism.

| Capability | Owner | Admin | Member |
|---|---:|---:|---:|
| View the board, its columns, tasks, members, and activity | Yes | Yes | Yes |
| Create or convert a board | Yes | No | No |
| Manage board settings, columns, and board tag definitions | Yes | Yes | No |
| Create or delete a top-level task | Yes | Yes | No |
| Edit any task / change its assignee | Yes | Yes | No |
| Edit an assigned task's title, description, tags, due date, or column | Yes | Yes | Assigned task only |
| Manage subtasks on an assigned task (create/delete/complete/reopen) | Yes | Yes | Assigned task only |
| Add a comment to any task visible in the board | Yes | Yes | Yes |
| Invite registered users | Yes | Yes | No |
| Change roles or remove non-Owner members | Yes | Yes | No |
| Change or remove the Owner | No self-removal in V1 | No | No |

## Invariants

- A board has one and only one Owner membership. Admin and Member are the only other board roles.
- Admin cannot promote a user to Owner, demote/remove the Owner, or change the Owner's board membership.
- `Task.assignee` is optional and singular. Only Owner/Admin may change it; an assignee must be a member of the task's board.
- Member task mutations require the current user to be the assignee. The only cross-assignment write permitted to a Member is adding a comment to a task they can view.
- Member task update DTOs reject fields outside the allowed set, even if a client sends them manually.
- Invitation acceptance creates the board membership transactionally. Pending or declined invitations do not grant board access.

## Expected denials

- Unauthenticated REST request: `401`.
- Authenticated user without board access: `404` or `403` according to the API's resource-disclosure policy, chosen consistently in the API contract.
- Board member attempting a role-forbidden mutation: `403`.
- Expired, revoked, or wrong-user invitation/reset token: generic invalid-token response; no state change.

The exact comment moderation policy and the exact `404` versus `403` policy are open contract decisions.
