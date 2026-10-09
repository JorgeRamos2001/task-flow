CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    username VARCHAR(40) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_name_nonblank CHECK (length(trim(name)) > 0),
    CONSTRAINT ck_users_username_normalized CHECK (username = lower(username)),
    CONSTRAINT ck_users_email_normalized CHECK (email = lower(email)),
    CONSTRAINT ck_users_username_nonblank CHECK (length(trim(username)) > 0),
    CONSTRAINT ck_users_email_nonblank CHECK (length(trim(email)) > 0)
);

CREATE TABLE oauth_identities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    provider VARCHAR(24) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_oauth_identities_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_oauth_identity_provider_subject UNIQUE (provider, provider_subject),
    CONSTRAINT ck_oauth_identities_provider CHECK (provider IN ('GOOGLE'))
);

CREATE INDEX ix_oauth_identities_user_id ON oauth_identities (user_id);

CREATE TABLE boards (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    mode VARCHAR(20) NOT NULL,
    theme_color VARCHAR(7) NOT NULL,
    owner_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_boards_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_boards_name_nonblank CHECK (length(trim(name)) > 0),
    CONSTRAINT ck_boards_mode CHECK (mode IN ('PERSONAL', 'COLLABORATIVE')),
    CONSTRAINT ck_boards_theme_color CHECK (theme_color ~ '^#[0-9A-Fa-f]{6}$')
);

CREATE INDEX ix_boards_owner_id ON boards (owner_id);

CREATE TABLE board_members (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_board_members_board FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
    CONSTRAINT fk_board_members_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_board_members_board_user UNIQUE (board_id, user_id),
    CONSTRAINT ck_board_members_role CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER'))
);

CREATE UNIQUE INDEX uq_board_members_single_owner
    ON board_members (board_id)
    WHERE role = 'OWNER';
CREATE INDEX ix_board_members_user_id ON board_members (user_id);

CREATE TABLE board_columns (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL,
    name VARCHAR(80) NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_board_columns_board FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
    CONSTRAINT uq_board_columns_board_position UNIQUE (board_id, position) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_board_columns_board_id UNIQUE (board_id, id),
    CONSTRAINT ck_board_columns_name_nonblank CHECK (length(trim(name)) > 0),
    CONSTRAINT ck_board_columns_position_nonnegative CHECK (position >= 0)
);

CREATE TABLE board_tags (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL,
    name VARCHAR(40) NOT NULL,
    normalized_name VARCHAR(40) NOT NULL,
    color VARCHAR(7) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_board_tags_board FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
    CONSTRAINT uq_board_tags_board_name UNIQUE (board_id, normalized_name),
    CONSTRAINT uq_board_tags_board_id UNIQUE (board_id, id),
    CONSTRAINT ck_board_tags_name_nonblank CHECK (length(trim(name)) > 0),
    CONSTRAINT ck_board_tags_color CHECK (color ~ '^#[0-9A-Fa-f]{6}$')
);

CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL,
    column_id UUID NOT NULL,
    created_by_user_id UUID,
    created_by_name_snapshot VARCHAR(120) NOT NULL,
    assigned_user_id UUID,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_at TIMESTAMPTZ,
    position INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_tasks_board FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_column FOREIGN KEY (board_id, column_id)
        REFERENCES board_columns (board_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_tasks_creator FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_tasks_assignee_membership FOREIGN KEY (board_id, assigned_user_id)
        REFERENCES board_members (board_id, user_id) ON DELETE RESTRICT,
    CONSTRAINT uq_tasks_board_id UNIQUE (board_id, id),
    CONSTRAINT uq_tasks_column_position UNIQUE (column_id, position) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT ck_tasks_title_nonblank CHECK (length(trim(title)) > 0),
    CONSTRAINT ck_tasks_position_nonnegative CHECK (position >= 0),
    CONSTRAINT ck_tasks_creator_snapshot_nonblank CHECK (length(trim(created_by_name_snapshot)) > 0)
);

CREATE INDEX ix_tasks_board_column_position ON tasks (board_id, column_id, position);
CREATE INDEX ix_tasks_assignee_due_at ON tasks (assigned_user_id, due_at) WHERE due_at IS NOT NULL;
CREATE INDEX ix_tasks_due_at ON tasks (due_at) WHERE due_at IS NOT NULL;

CREATE TABLE task_tags (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL,
    task_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_tags_task FOREIGN KEY (board_id, task_id)
        REFERENCES tasks (board_id, id) ON DELETE CASCADE,
    CONSTRAINT fk_task_tags_tag FOREIGN KEY (board_id, tag_id)
        REFERENCES board_tags (board_id, id) ON DELETE CASCADE,
    CONSTRAINT uq_task_tags_task_tag UNIQUE (task_id, tag_id)
);

CREATE INDEX ix_task_tags_tag_id ON task_tags (tag_id);

CREATE TABLE subtasks (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    position INTEGER NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_subtasks_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT uq_subtasks_task_position UNIQUE (task_id, position) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT ck_subtasks_title_nonblank CHECK (length(trim(title)) > 0),
    CONSTRAINT ck_subtasks_position_nonnegative CHECK (position >= 0),
    CONSTRAINT ck_subtasks_completion_timestamp CHECK ((completed AND completed_at IS NOT NULL) OR (NOT completed AND completed_at IS NULL))
);

CREATE TABLE comments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    author_user_id UUID,
    author_name_snapshot VARCHAR(120) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_comments_body_nonblank CHECK (length(trim(body)) > 0),
    CONSTRAINT ck_comments_author_snapshot_nonblank CHECK (length(trim(author_name_snapshot)) > 0)
);

CREATE INDEX ix_comments_task_created_at ON comments (task_id, created_at);

CREATE TABLE board_invitations (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL,
    invited_user_id UUID NOT NULL,
    invited_by_user_id UUID,
    inviter_name_snapshot VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_board_invitations_board FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
    CONSTRAINT fk_board_invitations_invited_user FOREIGN KEY (invited_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_board_invitations_inviter FOREIGN KEY (invited_by_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT uq_board_invitations_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_board_invitations_role CHECK (role IN ('ADMIN', 'MEMBER')),
    CONSTRAINT ck_board_invitations_status CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'REVOKED')),
    CONSTRAINT ck_board_invitations_token_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_board_invitations_expiry CHECK (expires_at > created_at),
    CONSTRAINT ck_board_invitations_inviter_snapshot_nonblank CHECK (length(trim(inviter_name_snapshot)) > 0)
);

CREATE UNIQUE INDEX uq_board_invitations_pending_user
    ON board_invitations (board_id, invited_user_id)
    WHERE status = 'PENDING';
CREATE INDEX ix_board_invitations_invited_status ON board_invitations (invited_user_id, status);

CREATE TABLE refresh_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_refresh_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_refresh_sessions_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_refresh_sessions_token_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_refresh_sessions_expiry CHECK (expires_at > created_at)
);

CREATE INDEX ix_refresh_sessions_user_expiry ON refresh_sessions (user_id, expires_at);

CREATE TABLE revoked_access_tokens (
    jti VARCHAR(100) PRIMARY KEY,
    user_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_revoked_access_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_revoked_access_tokens_expiry ON revoked_access_tokens (expires_at);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_password_reset_tokens_hash UNIQUE (token_hash),
    CONSTRAINT ck_password_reset_tokens_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_password_reset_tokens_expiry CHECK (expires_at > created_at)
);

CREATE INDEX ix_password_reset_tokens_user_expiry ON password_reset_tokens (user_id, expires_at);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL,
    board_id UUID,
    task_id UUID,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(160) NOT NULL,
    body TEXT,
    dedupe_key VARCHAR(160),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMPTZ,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_board FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE SET NULL,
    CONSTRAINT ck_notifications_type CHECK (type IN ('TASK_OVERDUE', 'TASK_ASSIGNED', 'BOARD_INVITATION', 'BOARD_ACTIVITY')),
    CONSTRAINT ck_notifications_title_nonblank CHECK (length(trim(title)) > 0)
);

CREATE UNIQUE INDEX uq_notifications_recipient_dedupe
    ON notifications (recipient_user_id, dedupe_key)
    WHERE dedupe_key IS NOT NULL;
CREATE INDEX ix_notifications_unread ON notifications (recipient_user_id, created_at DESC) WHERE read_at IS NULL;

CREATE TABLE board_activity (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL,
    actor_user_id UUID,
    actor_name_snapshot VARCHAR(120) NOT NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(32),
    target_id UUID,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_board_activity_board FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
    CONSTRAINT fk_board_activity_actor FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_board_activity_actor_snapshot_nonblank CHECK (length(trim(actor_name_snapshot)) > 0),
    CONSTRAINT ck_board_activity_action_nonblank CHECK (length(trim(action)) > 0)
);

CREATE INDEX ix_board_activity_board_created_at ON board_activity (board_id, created_at DESC);
