package com.taskflow.boards.entity;

import com.taskflow.shared.persistence.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "board_columns", uniqueConstraints = {
		@UniqueConstraint(name = "uq_board_columns_board_position", columnNames = {"board_id", "position"})
})
public class BoardColumn extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@Column(nullable = false, length = 80)
	private String name;

	@Column(nullable = false)
	private int position;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected BoardColumn() {
	}

	BoardColumn(Board board, String name, int position) {
		this.board = board;
		this.name = requireName(name);
		this.position = position;
	}

	public void rename(String name) {
		this.name = requireName(name);
	}

	void moveToPosition(int position) {
		if (position < 0) {
			throw new IllegalArgumentException("position must be non-negative");
		}
		this.position = position;
	}

	private static String requireName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		return name;
	}

	public Board getBoard() {
		return board;
	}

	public String getName() {
		return name;
	}

	public int getPosition() {
		return position;
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
