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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "like_management",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_like_management_uuid", columnNames = "like_uuid"),
				@UniqueConstraint(
						name = "uk_like_management_member_target",
						columnNames = {"member_uuid", "like_type", "target_uuid"}
				)
		},
		indexes = {
				@Index(name = "idx_like_management_target", columnList = "like_type, target_uuid, deleted_at"),
				@Index(name = "idx_like_management_member", columnList = "member_uuid, deleted_at, updated_at")
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

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "target_uuid", nullable = false, length = 36)
	private UUID targetUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "like_type", nullable = false, length = 20)
	private LikeType likeType;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected LikeManagementJpaEntity() {
	}

	LikeManagementJpaEntity(
			UUID likeUuid,
			UUID memberUuid,
			UUID targetUuid,
			LikeType likeType,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		this.likeUuid = likeUuid;
		this.memberUuid = memberUuid;
		this.targetUuid = targetUuid;
		this.likeType = likeType;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	void apply(Instant updatedAt, Instant deletedAt) {
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
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

	UUID targetUuid() {
		return targetUuid;
	}

	LikeType likeType() {
		return likeType;
	}

	Instant createdAt() {
		return createdAt;
	}

	Instant updatedAt() {
		return updatedAt;
	}

	Instant deletedAt() {
		return deletedAt;
	}
}
