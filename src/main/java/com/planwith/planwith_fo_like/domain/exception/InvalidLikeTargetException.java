package com.planwith.planwith_fo_like.domain.exception;

public class InvalidLikeTargetException extends LikeDomainException {

	public InvalidLikeTargetException(String message) {
		super("INVALID_LIKE_TARGET", message);
	}
}
