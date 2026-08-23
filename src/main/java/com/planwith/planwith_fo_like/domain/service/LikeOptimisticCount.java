package com.planwith.planwith_fo_like.domain.service;

public final class LikeOptimisticCount {

	private LikeOptimisticCount() {
	}

	public static long onLike(long currentCount) {
		if (currentCount < 0) {
			return 1L;
		}
		return currentCount + 1;
	}

	public static long onUnlike(long currentCount) {
		if (currentCount <= 0) {
			return 0L;
		}
		return currentCount - 1;
	}
}
