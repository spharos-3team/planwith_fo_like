package com.planwith.planwith_fo_like.domain.service;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeType;

public final class LikeTargetValidator {

	private LikeTargetValidator() {
	}

	public static void validate(LikeType likeType, UUID targetUuid) {
		LikeCommonValidator.validate(likeType, targetUuid);
	}

	public static void validate(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		LikeCommonValidator.validate(memberUuid, likeType, targetUuid);
	}
}
