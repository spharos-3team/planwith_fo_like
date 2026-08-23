package com.planwith.planwith_fo_like.domain.model;

import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTypeException;

public enum LikeType {
	STORY,
	COMMENT;

	public static LikeType from(String rawType) {
		if (rawType == null || rawType.isBlank()) {
			throw new InvalidLikeTypeException("좋아요 대상 타입이 없습니다.");
		}
		try {
			return LikeType.valueOf(rawType.trim().toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new InvalidLikeTypeException("지원하지 않는 좋아요 대상 타입입니다.");
		}
	}
}
