package com.taskflow.auth.entity;

import com.taskflow.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "revoked_access_tokens")
public class RevokedAccessToken {

	@Id
	@Column(name = "jti", nullable = false, length = 100)
	private String jti;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@CreationTimestamp
	@Column(name = "revoked_at", nullable = false, updatable = false)
	private Instant revokedAt;

	protected RevokedAccessToken() {
	}

	public RevokedAccessToken(String jti, User user, Instant expiresAt) {
		this.jti = requireJti(jti);
		this.user = Objects.requireNonNull(user, "user");
		this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
	}

	private static String requireJti(String jti) {
		if (jti == null || jti.isBlank() || jti.length() > 100) {
			throw new IllegalArgumentException("jti must contain 1 to 100 characters");
		}
		return jti;
	}

	public String getJti() {
		return jti;
	}

	public User getUser() {
		return user;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}
}
