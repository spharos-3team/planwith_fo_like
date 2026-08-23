package com.planwith.planwith_fo_like.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LikeOptimisticCountTest {

	@Test
	void onLikeIncrementsAndOnUnlikeFloorsAtZero() {
		assertThat(LikeOptimisticCount.onLike(152L)).isEqualTo(153L);
		assertThat(LikeOptimisticCount.onUnlike(153L)).isEqualTo(152L);
		assertThat(LikeOptimisticCount.onLike(0L)).isEqualTo(1L);
		assertThat(LikeOptimisticCount.onUnlike(0L)).isZero();
		assertThat(LikeOptimisticCount.onUnlike(-1L)).isZero();
		assertThat(LikeOptimisticCount.onLike(-1L)).isEqualTo(1L);
	}
}
