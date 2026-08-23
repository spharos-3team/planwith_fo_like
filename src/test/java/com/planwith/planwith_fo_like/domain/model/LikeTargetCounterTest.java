package com.planwith.planwith_fo_like.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class LikeTargetCounterTest {

	@Test
	void incrementAndDecrementKeepCountAndVersionSeparateFromZeroFloor() {
		LikeTargetCounter counter = LikeTargetCounter.create(TargetType.STORY, UUID.randomUUID());

		assertThat(counter.increment()).isEqualTo(1L);
		assertThat(counter.likeCount()).isEqualTo(1L);
		assertThat(counter.decrement()).isEqualTo(2L);
		assertThat(counter.likeCount()).isZero();
		assertThat(counter.decrement()).isEqualTo(3L);
		assertThat(counter.likeCount()).isZero();
	}
}
