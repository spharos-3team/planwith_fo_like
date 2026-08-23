package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_like.domain.model.LikeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "like_target_counter")
@IdClass(LikeTargetCounterId.class)
class LikeTargetCounterJpaEntity {

	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "like_type", nullable = false, length = 20)
	private LikeType likeType;

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "target_uuid", nullable = false, length = 36)
	private UUID targetUuid;

	@Column(name = "like_count", nullable = false)
	private long likeCount;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected LikeTargetCounterJpaEntity() {
	}

	LikeTargetCounterJpaEntity(LikeType likeType, UUID targetUuid, Instant updatedAt) {
		this.likeType = likeType;
		this.targetUuid = targetUuid;
		this.likeCount = 0L;
		this.updatedAt = updatedAt;
	}

	void increment(Instant now) {
		this.likeCount++;
		this.updatedAt = now;
	}

	void decrement(Instant now) {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
		this.updatedAt = now;
	}

	LikeType likeType() {
		return likeType;
	}

	UUID targetUuid() {
		return targetUuid;
	}

	long likeCount() {
		return likeCount;
	}

	Instant updatedAt() {
		return updatedAt;
	}
}
