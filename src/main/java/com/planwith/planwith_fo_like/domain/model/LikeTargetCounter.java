package com.planwith.planwith_fo_like.domain.model;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;

public final class LikeTargetCounter {

	private final Long counterId;
	private final TargetType targetType;
	private final UUID targetUuid;
	private long likeCount;
	private long version;

	private LikeTargetCounter(
			Long counterId,
			TargetType targetType,
			UUID targetUuid,
			long likeCount,
			long version
	) {
		if (targetType == null || targetUuid == null) {
			throw new InvalidLikeTargetException("좋아요 카운터 대상이 없습니다.");
		}
		if (likeCount < 0 || version < 0) {
			throw new InvalidLikeTargetException("좋아요 카운터 값이 올바르지 않습니다.");
		}
		this.counterId = counterId;
		this.targetType = targetType;
		this.targetUuid = targetUuid;
		this.likeCount = likeCount;
		this.version = version;
	}

	public static LikeTargetCounter create(TargetType targetType, UUID targetUuid) {
		return new LikeTargetCounter(null, targetType, targetUuid, 0L, 0L);
	}

	public static LikeTargetCounter restore(
			Long counterId,
			TargetType targetType,
			UUID targetUuid,
			long likeCount,
			long version
	) {
		return new LikeTargetCounter(counterId, targetType, targetUuid, likeCount, version);
	}

	public long increment() {
		this.likeCount++;
		this.version++;
		return this.version;
	}

	public long decrement() {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
		this.version++;
		return this.version;
	}

	public Long counterId() {
		return counterId;
	}

	public TargetType targetType() {
		return targetType;
	}

	public UUID targetUuid() {
		return targetUuid;
	}

	public long likeCount() {
		return likeCount;
	}

	public long version() {
		return version;
	}
}
