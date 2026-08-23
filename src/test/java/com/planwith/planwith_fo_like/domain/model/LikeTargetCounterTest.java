package com.planwith.planwith_fo_like.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class LikeTargetCounterTest {

	@Test
	void incrementAndDecrementKeepCountAndDoNotGoBelowZero() {
		Instant now = Instant.parse("2026-08-23T00:00:00Z");
		LikeTargetCounter counter = LikeTargetCounter.create(LikeType.STORY, UUID.randomUUID(), now);

		assertThat(counter.increment(now.plusSeconds(1))).isEqualTo(1L);
		assertThat(counter.likeCount()).isEqualTo(1L);
		assertThat(counter.decrement(now.plusSeconds(2))).isZero();
		assertThat(counter.likeCount()).isZero();
		assertThat(counter.decrement(now.plusSeconds(3))).isZero();
		assertThat(counter.likeCount()).isZero();
		assertThat(counter.likeType()).isEqualTo(LikeType.STORY);
	}
}
