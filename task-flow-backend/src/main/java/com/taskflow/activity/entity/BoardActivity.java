package com.taskflow.activity.entity;

import com.taskflow.boards.entity.Board;
import com.taskflow.shared.persistence.UuidEntity;
import com.taskflow.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "board_activity")
public class BoardActivity extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_user_id")
	private User actor;

	@Column(name = "actor_name_snapshot", nullable = false, length = 120)
	private String actorNameSnapshot;

	@Column(nullable = false, length = 64)
	private String action;

	@Column(name = "target_type", length = 32)
	private String targetType;

	@Column(name = "target_id")
	private UUID targetId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> metadata = new HashMap<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected BoardActivity() {
	}

	public BoardActivity(Board board, User actor, String actorNameSnapshot, String action, String targetType,
			UUID targetId, Map<String, Object> metadata) {
		this.board = Objects.requireNonNull(board, "board");
		this.actor = actor;
		this.actorNameSnapshot = requireText(actorNameSnapshot, "actorNameSnapshot");
		this.action = requireText(action, "action");
		this.targetType = targetType;
		this.targetId = targetId;
		this.metadata = metadata == null ? new HashMap<>() : new HashMap<>(metadata);
	}

	private static String requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
		return value;
	}

	public Board getBoard() {
		return board;
	}

	public User getActor() {
		return actor;
	}

	public String getActorNameSnapshot() {
		return actorNameSnapshot;
	}

	public String getAction() {
		return action;
	}

	public String getTargetType() {
		return targetType;
	}

	public UUID getTargetId() {
		return targetId;
	}

	public Map<String, Object> getMetadata() {
		return Map.copyOf(metadata);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
