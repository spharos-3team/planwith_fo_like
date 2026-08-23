package com.planwith.planwith_fo_like.domain.exception;

public class LikeDomainException extends RuntimeException {

	private final String errorCode;

	public LikeDomainException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public String errorCode() {
		return errorCode;
	}
}
