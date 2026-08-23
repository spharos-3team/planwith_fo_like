package com.planwith.planwith_fo_like.domain.exception;

public class InvalidLikeTypeException extends LikeDomainException {

	public InvalidLikeTypeException(String message) {
		super("INVALID_LIKE_TYPE", message);
	}
}
