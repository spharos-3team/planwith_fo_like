package com.planwith.planwith_fo_like.domain.exception;

public class DuplicateLikeException extends LikeDomainException {

	public DuplicateLikeException() {
		super("DUPLICATE_LIKE", "이미 좋아요한 대상입니다.");
	}
}
