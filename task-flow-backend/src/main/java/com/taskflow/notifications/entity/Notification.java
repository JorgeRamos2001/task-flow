package com.taskflow.notifications.entity;

import com.taskflow.boards.entity.Board;
import com.taskflow.shared.persistence.UuidEntity;
import com.taskflow.tasks.entity.Task;
import com.taskflow.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "notifications")
public class Notification extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipient_user_id", nullable = false)
	private User recipient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id")
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id")
	private Task task;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private NotificationType type;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(columnDefinition = "text")
	private String body;

	@Column(name = "dedupe_key", length = 160)
	private String dedupeKey;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "read_at")
	private Instant readAt;

	protected Notification() {
	}

	public Notification(User recipient, Board board, Task task, NotificationType type, String title, String body,
			String dedupeKey) {
		this.recipient = Objects.requireNonNull(recipient, "recipient");
		this.board = board;
		this.task = task;
		this.type = Objects.requireNonNull(type, "type");
		this.title = requireTitle(title);
		this.body = body;
		this.dedupeKey = dedupeKey;
	}

	public void markRead(Instant readAt) {
		if (this.readAt == null) {
			this.readAt = Objects.requireNonNull(readAt, "readAt");
		}
	}

	private static String requireTitle(String title) {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("title must not be blank");
		}
		return title;
	}

	public User getRecipient() {
		return recipient;
	}

	public Board getBoard() {
		return board;
	}

	public Task getTask() {
		return task;
	}

	public NotificationType getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public String getDedupeKey() {
		return dedupeKey;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getReadAt() {
		return readAt;
	}

	public boolean isRead() {
		return readAt != null;
	}
}
