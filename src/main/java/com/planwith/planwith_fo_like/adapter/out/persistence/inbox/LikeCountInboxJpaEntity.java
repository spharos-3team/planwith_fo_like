package com.planwith.planwith_fo_like.adapter.out.persistence.inbox;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "like_count_inbox")
class LikeCountInboxJpaEntity {

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "event_id", nullable = false, length = 36)
	private UUID eventId;

	@Column(name = "processed_at", nullable = false)
	private Instant processedAt;

	protected LikeCountInboxJpaEntity() {
	}

	LikeCountInboxJpaEntity(UUID eventId, Instant processedAt) {
		this.eventId = eventId;
		this.processedAt = processedAt;
	}
}
