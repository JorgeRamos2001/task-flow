package com.taskflow.users.entity;

import com.taskflow.shared.persistence.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "users")
public class User extends UuidEntity {

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, unique = true, length = 40)
	private String username;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	@Column(name = "password_hash", length = 100)
	private String passwordHash;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected User() {
	}

	public User(String name, String username, String email, String passwordHash) {
		this.name = requireDisplayName(name);
		this.username = normalizeIdentifier(username, "username");
		this.email = normalizeIdentifier(email, "email");
		this.passwordHash = passwordHash;
	}

	public void updateProfile(String name, String username) {
		this.name = requireDisplayName(name);
		this.username = normalizeIdentifier(username, "username");
	}

	public void changePassword(String passwordHash) {
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new IllegalArgumentException("passwordHash must not be blank");
		}
		this.passwordHash = passwordHash;
	}

	private static String requireDisplayName(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		return value.trim();
	}

	private static String normalizeIdentifier(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
		return value.trim().toLowerCase(Locale.ROOT);
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
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
