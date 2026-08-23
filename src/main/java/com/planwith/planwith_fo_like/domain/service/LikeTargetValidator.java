package com.planwith.planwith_fo_like.domain.service;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;
import com.planwith.planwith_fo_like.domain.model.TargetType;

public final class LikeTargetValidator {

	private LikeTargetValidator() {
	}

	public static void validate(TargetType targetType, UUID targetUuid, UUID targetOwnerUuid) {
		if (targetType == null) {
			throw new InvalidLikeTargetException("좋아요 대상 타입이 없습니다.");
		}
		if (targetUuid == null) {
			throw new InvalidLikeTargetException("좋아요 대상 식별자가 없습니다.");
		}
		if (targetOwnerUuid == null) {
			throw new InvalidLikeTargetException("좋아요 대상 작성자 식별자가 없습니다.");
		}
	}
}
