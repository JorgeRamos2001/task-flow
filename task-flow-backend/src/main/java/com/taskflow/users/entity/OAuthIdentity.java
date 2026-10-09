package com.taskflow.users.entity;

import com.taskflow.shared.persistence.UuidEntity;
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

@Entity
@Table(name = "oauth_identities", uniqueConstraints = {
		@UniqueConstraint(name = "uq_oauth_identity_provider_subject", columnNames = {"provider", "provider_subject"})
})
public class OAuthIdentity extends UuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private OAuthProvider provider;

	@Column(name = "provider_subject", nullable = false, length = 255)
	private String providerSubject;

	@CreationTimestamp
	@Column(name = "linked_at", nullable = false, updatable = false)
	private Instant linkedAt;

	protected OAuthIdentity() {
	}

	public OAuthIdentity(User user, OAuthProvider provider, String providerSubject) {
		this.user = user;
		this.provider = provider;
		this.providerSubject = providerSubject;
	}

	public User getUser() {
		return user;
	}

	public OAuthProvider getProvider() {
		return provider;
	}

	public String getProviderSubject() {
		return providerSubject;
	}

	public Instant getLinkedAt() {
		return linkedAt;
	}
}
