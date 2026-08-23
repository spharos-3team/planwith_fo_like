package com.planwith.planwith_fo_like.domain.service;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.exception.DuplicateLikeException;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeMemberException;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTypeException;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeManagementStatus;
import com.planwith.planwith_fo_like.domain.model.LikeType;

public final class LikeCommonValidator {

	private LikeCommonValidator() {
	}

	public static void validateMember(UUID memberUuid) {
		if (memberUuid == null) {
			throw new InvalidLikeMemberException("회원 식별자가 없습니다.");
		}
	}

	public static void validateType(LikeType likeType) {
		if (likeType == null) {
			throw new InvalidLikeTypeException("좋아요 대상 타입이 없습니다.");
		}
	}

	public static void validateTarget(UUID targetUuid) {
		if (targetUuid == null) {
			throw new InvalidLikeTargetException("좋아요 대상 식별자가 없습니다.");
		}
	}

	public static void validateOwner(UUID targetOwnerUuid) {
		if (targetOwnerUuid == null) {
			throw new InvalidLikeTargetException("대상 작성자 식별자가 없습니다.");
		}
	}

	public static void validate(LikeType likeType, UUID targetUuid) {
		validateType(likeType);
		validateTarget(targetUuid);
	}

	public static void validate(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		validateMember(memberUuid);
		validate(likeType, targetUuid);
	}

	public static void validateCommand(UUID memberUuid, LikeType likeType, UUID targetUuid, UUID targetOwnerUuid) {
		validate(memberUuid, likeType, targetUuid);
		validateOwner(targetOwnerUuid);
	}

	public static UUID parseUuid(String rawUuid, String message) {
		if (rawUuid == null || rawUuid.isBlank()) {
			throw new InvalidLikeTargetException(message);
		}
		try {
			return UUID.fromString(rawUuid.trim());
		} catch (IllegalArgumentException exception) {
			throw new InvalidLikeTargetException(message);
		}
	}

	public static LikeManagementStatus resolveStatus(Optional<LikeManagement> existing) {
		return LikeManagementStatus.from(existing);
	}

	public static LikeManagementStatus requireAddable(Optional<LikeManagement> existing) {
		LikeManagementStatus status = resolveStatus(existing);
		if (status.isDuplicateLike()) {
			throw new DuplicateLikeException();
		}
		return status;
	}

	public static Optional<LikeManagement> requireUnlikeable(Optional<LikeManagement> existing) {
		LikeManagementStatus status = resolveStatus(existing);
		if (!status.isUnlike()) {
			return Optional.empty();
		}
		return existing;
	}
}
