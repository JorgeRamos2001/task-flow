# TaskFlow V1 scope

## Product goal

Let individuals and small teams organize tasks in personal and collaborative boards, work within board-scoped roles, and see committed changes in real time.

## Included

- Email/username and password registration/login, Google OAuth2, refresh/logout, password recovery, Profile, and Account.
- Personal boards that can be converted to collaborative boards.
- Owner, Admin, and Member roles, invitations to registered users, and invitation acceptance/decline.
- Columns, tasks, one optional assignee, tags, due dates, subtasks, and comments.
- Board, List, and Timeline views; Home, My Tasks, task-only calendar, board activity, and in-app notification feed.
- Responsive desktop, tablet, and mobile web UI in English.
- Real-time events for collaborative board changes and scheduled overdue alerts.

## Excluded from V1

- A Workspace entity, tasks shared between boards, meeting/calendar events, or external calendar integrations.
- More than one assignee per task.
- Account sessions UI, two-factor authentication, appearance settings, notification preferences, language settings, and timezone settings.
- Invitations to unregistered email addresses.
- Due-soon reminder preferences; the required V1 alert is for overdue tasks.

## Confirmed account and board rules

- A board belongs to its creator and has exactly one Owner. A personal board initially contains only its Owner.
- Owner has absolute board authority. Admin can manage members other than the Owner, including invites, role changes, and removal. Admin cannot promote anyone to Owner or affect the Owner's membership.
- Member can view board tasks and comment on any task visible in that board. Member can manage an assigned task's title, description, tags, due date, and column, and can create, delete, complete, or reopen its subtasks.
- Member cannot create or delete top-level tasks, change a task's assignee, or manage board members/settings.
- Invitations target registered users, are persisted as pending, and require the invited user to accept or decline. No membership exists until acceptance.
- Password-reset requests return a generic response regardless of whether the email is registered. Reset tokens are high-entropy, single-use, short-lived, and stored as hashes.
- Account deletion is blocked while owned collaborative boards remain unresolved.

## Open decisions before their implementation

1. Define whether resolving an owned collaborative board means deleting it or adding an ownership-transfer workflow. V1 should not silently delete a shared board during account deletion.
2. Define whether task due dates are date-only or an exact instant. There is no timezone preference UI; recommended implementation is browser-local input converted to UTC for storage and comparison.
3. Define comment edit/delete policy. The V1 contract currently guarantees creating comments only.
4. Confirm invitation and reset-token lifetimes before implementation. Proposed defaults are 7 days for an invitation and 30 minutes for a password-reset token.
5. Select the production transactional-email provider. Local development uses Mailpit.
