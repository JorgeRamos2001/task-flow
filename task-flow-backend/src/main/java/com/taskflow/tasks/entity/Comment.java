package com.taskflow.tasks.entity;

import com.taskflow.shared.persistence.UuidEntity;
import com.taskflow.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "comments")
public class Comment extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_user_id")
	private User author;

	@Column(name = "author_name_snapshot", nullable = false, length = 120)
	private String authorNameSnapshot;

	@Column(nullable = false, columnDefinition = "text")
	private String body;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Comment() {
	}

	Comment(Task task, User author, String body) {
		this.task = Objects.requireNonNull(task, "task");
		this.author = Objects.requireNonNull(author, "author");
		this.authorNameSnapshot = author.getName();
		this.body = requireBody(body);
	}

	private static String requireBody(String body) {
		if (body == null || body.isBlank()) {
			throw new IllegalArgumentException("body must not be blank");
		}
		return body;
	}

	public Task getTask() {
		return task;
	}

	public User getAuthor() {
		return author;
	}

	public String getAuthorNameSnapshot() {
		return authorNameSnapshot;
	}

	public String getBody() {
		return body;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
