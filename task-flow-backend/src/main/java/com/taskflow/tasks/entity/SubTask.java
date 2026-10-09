package com.taskflow.tasks.entity;

import com.taskflow.shared.persistence.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "subtasks")
public class SubTask extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false)
	private int position;

	@Column(nullable = false)
	private boolean completed;

	@Column(name = "completed_at")
	private Instant completedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected SubTask() {
	}

	SubTask(Task task, String title, int position) {
		this.task = Objects.requireNonNull(task, "task");
		this.title = requireTitle(title);
		this.position = requirePosition(position);
	}

	public void rename(String title) {
		this.title = requireTitle(title);
	}

	public void setCompleted(boolean completed, Instant changedAt) {
		this.completed = completed;
		this.completedAt = completed ? Objects.requireNonNull(changedAt, "changedAt") : null;
	}

	private static String requireTitle(String title) {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("title must not be blank");
		}
		return title;
	}

	private static int requirePosition(int position) {
		if (position < 0) {
			throw new IllegalArgumentException("position must be non-negative");
		}
		return position;
	}

	public Task getTask() {
		return task;
	}

	public String getTitle() {
		return title;
	}

	public int getPosition() {
		return position;
	}

	public boolean isCompleted() {
		return completed;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public long getVersion() {
		return version;
	}
}
