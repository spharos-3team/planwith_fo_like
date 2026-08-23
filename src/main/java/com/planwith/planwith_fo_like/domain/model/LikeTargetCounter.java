package com.planwith.planwith_fo_like.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;

public final class LikeTargetCounter {

	private final LikeType likeType;
	private final UUID targetUuid;
	private long likeCount;
	private Instant updatedAt;

	private LikeTargetCounter(LikeType likeType, UUID targetUuid, long likeCount, Instant updatedAt) {
		if (likeType == null || targetUuid == null || updatedAt == null) {
			throw new InvalidLikeTargetException("좋아요 카운터 대상이 없습니다.");
		}
		if (likeCount < 0) {
			throw new InvalidLikeTargetException("좋아요 카운터 값이 올바르지 않습니다.");
		}
		this.likeType = likeType;
		this.targetUuid = targetUuid;
		this.likeCount = likeCount;
		this.updatedAt = updatedAt;
	}

	public static LikeTargetCounter create(LikeType likeType, UUID targetUuid, Instant now) {
		return new LikeTargetCounter(likeType, targetUuid, 0L, now);
	}

	public static LikeTargetCounter restore(LikeType likeType, UUID targetUuid, long likeCount, Instant updatedAt) {
		return new LikeTargetCounter(likeType, targetUuid, likeCount, updatedAt);
	}

	public long increment(Instant now) {
		this.likeCount++;
		this.updatedAt = requireNow(now);
		return this.likeCount;
	}

	public long decrement(Instant now) {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
		this.updatedAt = requireNow(now);
		return this.likeCount;
	}

	public LikeType likeType() {
		return likeType;
	}

	public UUID targetUuid() {
		return targetUuid;
	}

	public long likeCount() {
		return likeCount;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	private static Instant requireNow(Instant now) {
		if (now == null) {
			throw new InvalidLikeTargetException("좋아요 카운터 변경 시각이 없습니다.");
		}
		return now;
	}
}
