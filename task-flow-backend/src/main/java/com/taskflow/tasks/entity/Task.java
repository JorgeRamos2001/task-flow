package com.taskflow.tasks.entity;

import com.taskflow.boards.entity.Board;
import com.taskflow.boards.entity.BoardColumn;
import com.taskflow.boards.entity.BoardTag;
import com.taskflow.shared.persistence.UuidEntity;
import com.taskflow.users.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tasks")
public class Task extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "column_id", nullable = false)
	private BoardColumn column;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_user_id")
	private User createdBy;

	@Column(name = "created_by_name_snapshot", nullable = false, length = 120)
	private String createdByNameSnapshot;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_user_id")
	private User assignee;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "due_at")
	private Instant dueAt;

	@Column(nullable = false)
	private int position;

	@OneToMany(mappedBy = "task", cascade = CascadeType.PERSIST, orphanRemoval = true)
	@OrderBy("position ASC")
	private List<SubTask> subtasks = new ArrayList<>();

	@OneToMany(mappedBy = "task", cascade = CascadeType.PERSIST)
	@OrderBy("createdAt ASC")
	private List<Comment> comments = new ArrayList<>();

	@OneToMany(mappedBy = "task", cascade = CascadeType.PERSIST, orphanRemoval = true)
	private List<TaskTag> taskTags = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected Task() {
	}

	public Task(Board board, BoardColumn column, User createdBy, User assignee, String title, String description,
			Instant dueAt, int position) {
		this.board = Objects.requireNonNull(board, "board");
		this.column = Objects.requireNonNull(column, "column");
		this.createdBy = Objects.requireNonNull(createdBy, "createdBy");
		this.createdByNameSnapshot = createdBy.getName();
		this.assignee = assignee;
		this.title = requireTitle(title);
		this.description = description;
		this.dueAt = dueAt;
		this.position = requirePosition(position);
	}

	public void editContent(String title, String description, Instant dueAt) {
		this.title = requireTitle(title);
		this.description = description;
		this.dueAt = dueAt;
	}

	public void assignTo(User assignee) {
		this.assignee = assignee;
	}

	public void moveTo(BoardColumn column, int position) {
		this.column = Objects.requireNonNull(column, "column");
		this.position = requirePosition(position);
	}

	public SubTask addSubTask(String title, int position) {
		SubTask subTask = new SubTask(this, title, position);
		subtasks.add(subTask);
		return subTask;
	}

	public void removeSubTask(SubTask subTask) {
		if (subTask == null || !Objects.equals(subTask.getTask().getId(), getId()) || !subtasks.remove(subTask)) {
			throw new IllegalArgumentException("Subtask does not belong to this task");
		}
	}

	public Comment addComment(User author, String body) {
		Comment comment = new Comment(this, author, body);
		comments.add(comment);
		return comment;
	}

	public TaskTag addTag(BoardTag tag) {
		if (!Objects.equals(tag.getBoard().getId(), board.getId())) {
			throw new IllegalArgumentException("Task tags must belong to the task's board");
		}
		TaskTag association = new TaskTag(this, tag);
		taskTags.add(association);
		return association;
	}

	public void removeTag(TaskTag association) {
		if (association == null || !Objects.equals(association.getTask().getId(), getId())
				|| !taskTags.remove(association)) {
			throw new IllegalArgumentException("Tag association does not belong to this task");
		}
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

	public Board getBoard() {
		return board;
	}

	public BoardColumn getColumn() {
		return column;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public String getCreatedByNameSnapshot() {
		return createdByNameSnapshot;
	}

	public User getAssignee() {
		return assignee;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Instant getDueAt() {
		return dueAt;
	}

	public int getPosition() {
		return position;
	}

	public List<SubTask> getSubtasks() {
		return Collections.unmodifiableList(subtasks);
	}

	public List<Comment> getComments() {
		return Collections.unmodifiableList(comments);
	}

	public List<TaskTag> getTaskTags() {
		return Collections.unmodifiableList(taskTags);
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
