package com.planwith.planwith_fo_like.domain.event;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.exception.LikeDomainException;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeType;

public record LikeEvent(
		UUID eventId,
		LikeEventType eventType,
		UUID memberUuid,
		UUID likeUuid,
		LikeType likeType,
		UUID targetUuid,
		Instant occurredAt
) {

	public LikeEvent {
		if (eventId == null || eventType == null || memberUuid == null || likeUuid == null
				|| likeType == null || targetUuid == null || occurredAt == null) {
			throw new LikeDomainException("INVALID_LIKE_EVENT", "좋아요 이벤트 필수값이 없습니다.");
		}
	}

	public static LikeEvent like(LikeManagement like, Instant occurredAt) {
		return from(LikeEventType.LIKE, like, occurredAt);
	}

	public static LikeEvent unlike(LikeManagement like, Instant occurredAt) {
		return from(LikeEventType.UNLIKE, like, occurredAt);
	}

	private static LikeEvent from(LikeEventType eventType, LikeManagement like, Instant occurredAt) {
		if (like == null) {
			throw new LikeDomainException("INVALID_LIKE_EVENT", "좋아요 이벤트 대상이 없습니다.");
		}
		return new LikeEvent(
				UUID.randomUUID(),
				eventType,
				like.memberUuid(),
				like.likeUuid(),
				like.likeType(),
				like.targetUuid(),
				occurredAt
		);
	}

	public boolean isStoryLike() {
		return likeType == LikeType.STORY;
	}

	public boolean isCommentLike() {
		return likeType == LikeType.COMMENT;
	}
}
