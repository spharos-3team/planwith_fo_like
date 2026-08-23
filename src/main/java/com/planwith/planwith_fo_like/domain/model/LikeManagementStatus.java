package com.planwith.planwith_fo_like.domain.model;

import java.util.Optional;

public enum LikeManagementStatus {
	NEW_LIKE,
	RE_LIKE,
	UNLIKE;

	public static LikeManagementStatus from(Optional<LikeManagement> existing) {
		if (existing == null || existing.isEmpty()) {
			return NEW_LIKE;
		}
		LikeManagement like = existing.get();
		if (like.isDeleted()) {
			return RE_LIKE;
		}
		return UNLIKE;
	}

	public boolean isNewLike() {
		return this == NEW_LIKE;
	}

	public boolean isReLike() {
		return this == RE_LIKE;
	}

	public boolean isUnlike() {
		return this == UNLIKE;
	}

	public boolean isDuplicateLike() {
		return this == UNLIKE;
	}
}
