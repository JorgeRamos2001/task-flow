package com.taskflow.boards.entity;

import com.taskflow.shared.persistence.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "board_tags", uniqueConstraints = {
		@UniqueConstraint(name = "uq_board_tags_board_name", columnNames = {"board_id", "normalized_name"})
})
public class BoardTag extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@Column(nullable = false, length = 40)
	private String name;

	@Column(name = "normalized_name", nullable = false, length = 40)
	private String normalizedName;

	@Column(nullable = false, length = 7)
	private String color;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected BoardTag() {
	}

	public BoardTag(Board board, String name, String color) {
		this.board = Objects.requireNonNull(board, "board");
		updateName(name);
		this.color = requireColor(color);
	}

	public void updateName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		this.name = name.trim();
		this.normalizedName = this.name.toLowerCase(java.util.Locale.ROOT);
	}

	public void updateColor(String color) {
		this.color = requireColor(color);
	}

	private static String requireColor(String color) {
		if (color == null || !color.matches("#[0-9A-Fa-f]{6}")) {
			throw new IllegalArgumentException("color must be a six-digit hex color");
		}
		return color;
	}

	public Board getBoard() {
		return board;
	}

	public String getName() {
		return name;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	public String getColor() {
		return color;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
