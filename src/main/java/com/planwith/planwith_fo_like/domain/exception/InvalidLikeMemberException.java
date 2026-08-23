package com.planwith.planwith_fo_like.domain.exception;

public class InvalidLikeMemberException extends LikeDomainException {

	public InvalidLikeMemberException(String message) {
		super("INVALID_MEMBER", message);
	}
}
