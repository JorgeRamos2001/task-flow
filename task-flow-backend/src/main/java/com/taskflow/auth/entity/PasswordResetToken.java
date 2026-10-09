package com.taskflow.auth.entity;

import com.taskflow.shared.persistence.UuidEntity;
import com.taskflow.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "used_at")
	private Instant usedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected PasswordResetToken() {
	}

	public PasswordResetToken(User user, String tokenHash, Instant expiresAt) {
		this.user = Objects.requireNonNull(user, "user");
		this.tokenHash = requireHash(tokenHash);
		this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
	}

	public boolean isUsableAt(Instant now) {
		return usedAt == null && now.isBefore(expiresAt);
	}

	public void consume(Instant now) {
		if (!isUsableAt(now)) {
			throw new IllegalStateException("Password reset token is expired or already consumed");
		}
		usedAt = Objects.requireNonNull(now, "now");
	}

	private static String requireHash(String hash) {
		if (hash == null || !hash.matches("[0-9a-fA-F]{64}")) {
			throw new IllegalArgumentException("tokenHash must be a SHA-256 hex digest");
		}
		return hash.toLowerCase(java.util.Locale.ROOT);
	}

	public User getUser() {
		return user;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getUsedAt() {
		return usedAt;
	}

	public long getVersion() {
		return version;
	}
}
