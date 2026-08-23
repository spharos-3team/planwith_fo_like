package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_like.domain.model.TargetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "like_target_counter",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_like_target_counter_target",
						columnNames = {"target_type", "target_uuid"}
				)
		}
)
class LikeTargetCounterJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "counter_id")
	private Long counterId;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, length = 20)
	private TargetType targetType;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "target_uuid", nullable = false, length = 36)
	private UUID targetUuid;

	@Column(name = "like_count", nullable = false)
	private long likeCount;

	@Column(name = "version", nullable = false)
	private long version;

	protected LikeTargetCounterJpaEntity() {
	}

	LikeTargetCounterJpaEntity(TargetType targetType, UUID targetUuid) {
		this.targetType = targetType;
		this.targetUuid = targetUuid;
		this.likeCount = 0L;
	}

	void increment() {
		this.likeCount++;
		this.version++;
	}

	void decrement() {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
		this.version++;
	}

	Long counterId() {
		return counterId;
	}

	TargetType targetType() {
		return targetType;
	}

	UUID targetUuid() {
		return targetUuid;
	}

	long likeCount() {
		return likeCount;
	}

	long version() {
		return version;
	}
}
