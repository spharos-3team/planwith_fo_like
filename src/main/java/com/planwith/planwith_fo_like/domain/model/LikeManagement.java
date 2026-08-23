package com.planwith.planwith_fo_like.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;

public final class LikeManagement {

	private final Long likeId;
	private final UUID likeUuid;
	private final UUID memberUuid;
	private final UUID targetUuid;
	private final LikeType likeType;
	private final Instant createdAt;
	private Instant updatedAt;
	private Instant deletedAt;

	private LikeManagement(
			Long likeId,
			UUID likeUuid,
			UUID memberUuid,
			UUID targetUuid,
			LikeType likeType,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		if (likeUuid == null || memberUuid == null || targetUuid == null || likeType == null
				|| createdAt == null || updatedAt == null) {
			throw new InvalidLikeTargetException("좋아요 관계 필수값이 없습니다.");
		}
		this.likeId = likeId;
		this.likeUuid = likeUuid;
		this.memberUuid = memberUuid;
		this.targetUuid = targetUuid;
		this.likeType = likeType;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	public static LikeManagement create(UUID memberUuid, UUID targetUuid, LikeType likeType, Instant now) {
		return new LikeManagement(null, UUID.randomUUID(), memberUuid, targetUuid, likeType, now, now, null);
	}

	public static LikeManagement restore(
			Long likeId,
			UUID likeUuid,
			UUID memberUuid,
			UUID targetUuid,
			LikeType likeType,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		return new LikeManagement(
				likeId,
				likeUuid,
				memberUuid,
				targetUuid,
				likeType,
				createdAt,
				updatedAt,
				deletedAt
		);
	}

	public void markDeleted(Instant now) {
		if (now == null) {
			throw new InvalidLikeTargetException("좋아요 삭제 시각이 없습니다.");
		}
		this.deletedAt = now;
		this.updatedAt = now;
	}

	public void restoreDeleted(Instant now) {
		if (now == null) {
			throw new InvalidLikeTargetException("좋아요 복원 시각이 없습니다.");
		}
		this.deletedAt = null;
		this.updatedAt = now;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public boolean isActive() {
		return deletedAt == null;
	}

	public boolean isStoryLike() {
		return likeType == LikeType.STORY;
	}

	public boolean isCommentLike() {
		return likeType == LikeType.COMMENT;
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

	public UUID targetUuid() {
		return targetUuid;
	}

	public LikeType likeType() {
		return likeType;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	public Instant deletedAt() {
		return deletedAt;
	}
}
