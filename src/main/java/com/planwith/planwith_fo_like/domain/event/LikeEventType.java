package com.planwith.planwith_fo_like.domain.event;

public enum LikeEventType {
	LIKE,
	UNLIKE;

	public boolean isLike() {
		return this == LIKE;
	}

	public boolean isUnlike() {
		return this == UNLIKE;
	}
}
