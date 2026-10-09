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
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "board_invitations")
public class BoardInvitation extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invited_user_id", nullable = false)
	private User invitedUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invited_by_user_id")
	private User invitedBy;

	@Column(name = "inviter_name_snapshot", nullable = false, length = 120)
	private String inviterNameSnapshot;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BoardRole role;

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private InvitationStatus status = InvitationStatus.PENDING;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "responded_at")
	private Instant respondedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected BoardInvitation() {
	}

	public BoardInvitation(Board board, User invitedUser, User invitedBy, BoardRole role, String tokenHash,
			Instant expiresAt) {
		this.board = Objects.requireNonNull(board, "board");
		this.invitedUser = Objects.requireNonNull(invitedUser, "invitedUser");
		this.invitedBy = Objects.requireNonNull(invitedBy, "invitedBy");
		this.inviterNameSnapshot = invitedBy.getName();
		if (role == BoardRole.OWNER) {
			throw new IllegalArgumentException("Invitations cannot grant the Owner role");
		}
		this.role = Objects.requireNonNull(role, "role");
		this.tokenHash = requireText(tokenHash, "tokenHash");
		this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
	}

	public void accept(Instant now) {
		respond(InvitationStatus.ACCEPTED, now);
	}

	public void decline(Instant now) {
		respond(InvitationStatus.DECLINED, now);
	}

	public void revoke(Instant now) {
		respond(InvitationStatus.REVOKED, now);
	}

	public void expire(Instant now) {
		if (status == InvitationStatus.PENDING && !now.isBefore(expiresAt)) {
			status = InvitationStatus.EXPIRED;
			respondedAt = now;
		}
	}

	private void respond(InvitationStatus response, Instant now) {
		if (status != InvitationStatus.PENDING || !now.isBefore(expiresAt)) {
			throw new IllegalStateException("Invitation is no longer pending");
		}
		status = response;
		respondedAt = now;
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

	public User getInvitedUser() {
		return invitedUser;
	}

	public User getInvitedBy() {
		return invitedBy;
	}

	public String getInviterNameSnapshot() {
		return inviterNameSnapshot;
	}

	public BoardRole getRole() {
		return role;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public InvitationStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getRespondedAt() {
		return respondedAt;
	}

	public long getVersion() {
		return version;
	}
}
