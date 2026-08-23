package com.planwith.planwith_fo_like.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTypeException;

class LikeTypeTest {

	@Test
	void fromAcceptsStoryAndCommentOnly() {
		assertThat(LikeType.from("STORY")).isEqualTo(LikeType.STORY);
		assertThat(LikeType.from(" comment ")).isEqualTo(LikeType.COMMENT);
		assertThatThrownBy(() -> LikeType.from("PLAN")).isInstanceOf(InvalidLikeTypeException.class);
		assertThatThrownBy(() -> LikeType.from(" ")).isInstanceOf(InvalidLikeTypeException.class);
		assertThatThrownBy(() -> LikeType.from(null)).isInstanceOf(InvalidLikeTypeException.class);
	}
}
