package com.taskflow.boards.entity;

import com.taskflow.shared.persistence.UuidEntity;
import com.taskflow.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "board_members", uniqueConstraints = {
		@UniqueConstraint(name = "uq_board_members_board_user", columnNames = {"board_id", "user_id"})
})
public class BoardMember extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BoardRole role;

	@CreationTimestamp
	@Column(name = "joined_at", nullable = false, updatable = false)
	private Instant joinedAt;

	protected BoardMember() {
	}

	BoardMember(Board board, User user, BoardRole role) {
		this.board = Objects.requireNonNull(board, "board");
		this.user = Objects.requireNonNull(user, "user");
		this.role = Objects.requireNonNull(role, "role");
	}

	public void changeRole(BoardRole role) {
		if (this.role == BoardRole.OWNER || role == BoardRole.OWNER) {
			throw new IllegalArgumentException("Owner membership cannot be changed through member role updates");
		}
		this.role = Objects.requireNonNull(role, "role");
	}

	public Board getBoard() {
		return board;
	}

	public User getUser() {
		return user;
	}

	public BoardRole getRole() {
		return role;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}
}
