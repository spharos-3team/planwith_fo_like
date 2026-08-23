package com.planwith.planwith_fo_like.domain.service;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;
import com.planwith.planwith_fo_like.domain.model.LikeType;

public final class LikeTargetValidator {

	private LikeTargetValidator() {
	}

	public static void validate(LikeType likeType, UUID targetUuid) {
		if (likeType == null) {
			throw new InvalidLikeTargetException("좋아요 대상 타입이 없습니다.");
		}
		if (targetUuid == null) {
			throw new InvalidLikeTargetException("좋아요 대상 식별자가 없습니다.");
		}
	}

	public static void validate(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		if (memberUuid == null) {
			throw new InvalidLikeTargetException("회원 식별자가 없습니다.");
		}
		validate(likeType, targetUuid);
	}
}
