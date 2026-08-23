package com.planwith.planwith_fo_like.application.query;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeType;
import com.planwith.planwith_fo_like.domain.service.LikeOptimisticCount;

public record LikeSnapshotView(
		LikeType likeType,
		UUID targetUuid,
		boolean liked,
		long likeCount,
		long optimisticLikeCount,
		long optimisticUnlikeCount
) {

	public static LikeSnapshotView of(LikeType likeType, UUID targetUuid, boolean liked, long likeCount) {
		return new LikeSnapshotView(
				likeType,
				targetUuid,
				liked,
				likeCount,
				LikeOptimisticCount.onLike(likeCount),
				LikeOptimisticCount.onUnlike(likeCount)
		);
	}
}
