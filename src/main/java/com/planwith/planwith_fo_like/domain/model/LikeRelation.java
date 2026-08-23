package com.planwith.planwith_fo_like.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;

public final class LikeRelation {

	private final Long likeId;
	private final UUID likeUuid;
	private final UUID memberUuid;
	private final TargetType targetType;
	private final UUID targetUuid;
	private final UUID targetOwnerUuid;
	private final Instant createdAt;

	private LikeRelation(
			Long likeId,
			UUID likeUuid,
			UUID memberUuid,
			TargetType targetType,
			UUID targetUuid,
			UUID targetOwnerUuid,
			Instant createdAt
	) {
		if (likeUuid == null || memberUuid == null || targetType == null
				|| targetUuid == null || targetOwnerUuid == null || createdAt == null) {
			throw new InvalidLikeTargetException("좋아요 관계 필수값이 없습니다.");
		}
		this.likeId = likeId;
		this.likeUuid = likeUuid;
		this.memberUuid = memberUuid;
		this.targetType = targetType;
		this.targetUuid = targetUuid;
		this.targetOwnerUuid = targetOwnerUuid;
		this.createdAt = createdAt;
	}

	public static LikeRelation create(
			UUID memberUuid,
			TargetType targetType,
			UUID targetUuid,
			UUID targetOwnerUuid,
			Instant createdAt
	) {
		return new LikeRelation(
				null,
				UUID.randomUUID(),
				memberUuid,
				targetType,
				targetUuid,
				targetOwnerUuid,
				createdAt
		);
	}

	public static LikeRelation restore(
			Long likeId,
			UUID likeUuid,
			UUID memberUuid,
			TargetType targetType,
			UUID targetUuid,
			UUID targetOwnerUuid,
			Instant createdAt
	) {
		return new LikeRelation(likeId, likeUuid, memberUuid, targetType, targetUuid, targetOwnerUuid, createdAt);
	}

	public Long likeId() {
		return likeId;
	}

	public UUID likeUuid() {
		return likeUuid;
	}

	public UUID memberUuid() {
		return memberUuid;
	}

	public TargetType targetType() {
		return targetType;
	}

	public UUID targetUuid() {
		return targetUuid;
	}

	public UUID targetOwnerUuid() {
		return targetOwnerUuid;
	}

	public Instant createdAt() {
		return createdAt;
	}
}
