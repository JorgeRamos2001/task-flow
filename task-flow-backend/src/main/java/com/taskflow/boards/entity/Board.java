package com.taskflow.boards.entity;

import com.taskflow.shared.persistence.UuidEntity;
import com.taskflow.users.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "boards")
public class Board extends UuidEntity {

	@Column(nullable = false, length = 120)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BoardMode mode;

	@Column(name = "theme_color", nullable = false, length = 7)
	private String themeColor;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
	private List<BoardMember> members = new ArrayList<>();

	@OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
	@OrderBy("position ASC")
	private List<BoardColumn> columns = new ArrayList<>();

	@OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
	private List<BoardTag> tags = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected Board() {
	}

	public Board(User owner, String name, String description, BoardMode mode, String themeColor) {
		this.owner = Objects.requireNonNull(owner, "owner");
		this.name = requireText(name, "name");
		this.description = description;
		this.mode = Objects.requireNonNull(mode, "mode");
		this.themeColor = requireColor(themeColor);

		members.add(new BoardMember(this, owner, BoardRole.OWNER));
		addColumn("Backlog");
		addColumn("To Do");
		addColumn("In Progress");
		addColumn("Done");
	}

	public void convertToCollaborative() {
		this.mode = BoardMode.COLLABORATIVE;
	}

	public void rename(String name) {
		this.name = requireText(name, "name");
	}

	public void updateDescription(String description) {
		this.description = description;
	}

	public void updateThemeColor(String themeColor) {
		this.themeColor = requireColor(themeColor);
	}

	public BoardColumn addColumn(String columnName) {
		BoardColumn column = new BoardColumn(this, columnName, columns.size());
		columns.add(column);
		return column;
	}

	public BoardTag addTag(String tagName, String color) {
		BoardTag tag = new BoardTag(this, tagName, color);
		tags.add(tag);
		return tag;
	}

	public BoardMember addMember(User user, BoardRole role) {
		Objects.requireNonNull(user, "user");
		Objects.requireNonNull(role, "role");
		if (role == BoardRole.OWNER) {
			throw new IllegalArgumentException("The original board owner cannot be reassigned");
		}
		if (mode == BoardMode.PERSONAL) {
			throw new IllegalStateException("Convert the board to collaborative before adding members");
		}
		BoardMember member = new BoardMember(this, user, role);
		members.add(member);
		return member;
	}

	private static String requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
		return value;
	}

	private static String requireColor(String color) {
		if (color == null || !color.matches("#[0-9A-Fa-f]{6}")) {
			throw new IllegalArgumentException("themeColor must be a six-digit hex color");
		}
		return color;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public BoardMode getMode() {
		return mode;
	}

	public String getThemeColor() {
		return themeColor;
	}

	public User getOwner() {
		return owner;
	}

	public List<BoardMember> getMembers() {
		return Collections.unmodifiableList(members);
	}

	public List<BoardColumn> getColumns() {
		return Collections.unmodifiableList(columns);
	}

	public List<BoardTag> getTags() {
		return Collections.unmodifiableList(tags);
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
