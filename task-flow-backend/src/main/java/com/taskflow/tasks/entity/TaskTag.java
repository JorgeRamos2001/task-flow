package com.taskflow.tasks.entity;

import com.taskflow.boards.entity.Board;
import com.taskflow.boards.entity.BoardTag;
import com.taskflow.shared.persistence.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "task_tags", uniqueConstraints = {
		@UniqueConstraint(name = "uq_task_tags_task_tag", columnNames = {"task_id", "tag_id"})
})
public class TaskTag extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tag_id", nullable = false)
	private BoardTag tag;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected TaskTag() {
	}

	TaskTag(Task task, BoardTag tag) {
		this.task = Objects.requireNonNull(task, "task");
		this.tag = Objects.requireNonNull(tag, "tag");
		this.board = task.getBoard();
		if (!Objects.equals(this.board.getId(), tag.getBoard().getId())) {
			throw new IllegalArgumentException("Task tag must belong to the task's board");
		}
	}

	public Board getBoard() {
		return board;
	}

	public Task getTask() {
		return task;
	}

	public BoardTag getTag() {
		return tag;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
