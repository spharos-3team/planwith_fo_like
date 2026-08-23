package com.planwith.planwith_fo_like.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_like.domain.exception.DuplicateLikeException;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeMemberException;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTypeException;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeManagementStatus;
import com.planwith.planwith_fo_like.domain.model.LikeType;

class LikeCommonValidatorTest {

	private static final Instant NOW = Instant.parse("2026-08-23T00:00:00Z");

	@Test
	void validateMemberTargetAndType() {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();

		LikeCommonValidator.validateCommand(memberUuid, LikeType.STORY, targetUuid, ownerUuid);
		LikeCommonValidator.validate(memberUuid, LikeType.COMMENT, targetUuid);

		assertThatThrownBy(() -> LikeCommonValidator.validateMember(null))
				.isInstanceOf(InvalidLikeMemberException.class)
				.hasMessage("회원 식별자가 없습니다.");
		assertThatThrownBy(() -> LikeCommonValidator.validateTarget(null))
				.isInstanceOf(InvalidLikeTargetException.class)
				.hasMessage("좋아요 대상 식별자가 없습니다.");
		assertThatThrownBy(() -> LikeCommonValidator.validateType(null))
				.isInstanceOf(InvalidLikeTypeException.class)
				.hasMessage("좋아요 대상 타입이 없습니다.");
		assertThatThrownBy(() -> LikeCommonValidator.validateOwner(null))
				.isInstanceOf(InvalidLikeTargetException.class)
				.hasMessage("대상 작성자 식별자가 없습니다.");
	}

	@Test
	void parseUuidRejectsBlankAndInvalidFormat() {
		UUID uuid = UUID.randomUUID();

		assertThat(LikeCommonValidator.parseUuid(uuid.toString(), "대상 식별자가 올바르지 않습니다.")).isEqualTo(uuid);
		assertThatThrownBy(() -> LikeCommonValidator.parseUuid(" ", "대상 식별자가 올바르지 않습니다."))
				.isInstanceOf(InvalidLikeTargetException.class);
		assertThatThrownBy(() -> LikeCommonValidator.parseUuid("not-uuid", "대상 식별자가 올바르지 않습니다."))
				.isInstanceOf(InvalidLikeTargetException.class);
	}

	@Test
	void resolveStatusDistinguishesNewReLikeAndUnlike() {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		LikeManagement active = LikeManagement.create(memberUuid, targetUuid, LikeType.STORY, NOW);
		LikeManagement cancelled = LikeManagement.create(memberUuid, targetUuid, LikeType.COMMENT, NOW);
		cancelled.markDeleted(NOW.plusSeconds(1));

		assertThat(LikeCommonValidator.resolveStatus(Optional.empty())).isEqualTo(LikeManagementStatus.NEW_LIKE);
		assertThat(LikeCommonValidator.resolveStatus(Optional.of(cancelled))).isEqualTo(LikeManagementStatus.RE_LIKE);
		assertThat(LikeCommonValidator.resolveStatus(Optional.of(active))).isEqualTo(LikeManagementStatus.UNLIKE);

		assertThat(LikeCommonValidator.requireAddable(Optional.empty())).isEqualTo(LikeManagementStatus.NEW_LIKE);
		assertThat(LikeCommonValidator.requireAddable(Optional.of(cancelled))).isEqualTo(LikeManagementStatus.RE_LIKE);
		assertThatThrownBy(() -> LikeCommonValidator.requireAddable(Optional.of(active)))
				.isInstanceOf(DuplicateLikeException.class);

		assertThat(LikeCommonValidator.requireUnlikeable(Optional.of(active))).contains(active);
		assertThat(LikeCommonValidator.requireUnlikeable(Optional.of(cancelled))).isEmpty();
		assertThat(LikeCommonValidator.requireUnlikeable(Optional.empty())).isEmpty();
	}

	@Test
	void storyAndCommentAreIndependentOnSameTargetUuid() {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		LikeManagement storyLike = LikeManagement.create(memberUuid, targetUuid, LikeType.STORY, NOW);
		LikeManagement commentLike = LikeManagement.create(memberUuid, targetUuid, LikeType.COMMENT, NOW);
		commentLike.markDeleted(NOW.plusSeconds(1));

		assertThat(LikeCommonValidator.resolveStatus(Optional.of(storyLike))).isEqualTo(LikeManagementStatus.UNLIKE);
		assertThat(LikeCommonValidator.resolveStatus(Optional.of(commentLike))).isEqualTo(LikeManagementStatus.RE_LIKE);
	}
}
