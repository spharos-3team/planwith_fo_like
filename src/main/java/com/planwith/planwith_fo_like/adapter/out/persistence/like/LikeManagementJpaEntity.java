package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.time.Instant;
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

/**
 * 좋아요 관계 원본 테이블.
 * UNIQUE(member_uuid, target_type, target_uuid)는 최종 중복 좋아요 방어선이다.
 */
@Entity
@Table(
		name = "like_management",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_like_management_uuid", columnNames = "like_uuid"),
				@UniqueConstraint(
						name = "uk_like_management_member_target",
						columnNames = {"member_uuid", "target_type", "target_uuid"}
				)
		}
)
class LikeManagementJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "like_id")
	private Long likeId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "like_uuid", nullable = false, length = 36)
	private UUID likeUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, length = 20)
	private TargetType targetType;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "target_uuid", nullable = false, length = 36)
	private UUID targetUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "target_owner_uuid", nullable = false, length = 36)
	private UUID targetOwnerUuid;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected LikeManagementJpaEntity() {
	}

	LikeManagementJpaEntity(
			UUID likeUuid,
			UUID memberUuid,
			TargetType targetType,
			UUID targetUuid,
			UUID targetOwnerUuid,
			Instant createdAt
	) {
		this.likeUuid = likeUuid;
		this.memberUuid = memberUuid;
		this.targetType = targetType;
		this.targetUuid = targetUuid;
		this.targetOwnerUuid = targetOwnerUuid;
		this.createdAt = createdAt;
	}

	Long likeId() {
		return likeId;
	}

	UUID likeUuid() {
		return likeUuid;
	}

	UUID memberUuid() {
		return memberUuid;
	}

	TargetType targetType() {
		return targetType;
	}

	UUID targetUuid() {
		return targetUuid;
	}

	UUID targetOwnerUuid() {
		return targetOwnerUuid;
	}

	Instant createdAt() {
		return createdAt;
	}
}
