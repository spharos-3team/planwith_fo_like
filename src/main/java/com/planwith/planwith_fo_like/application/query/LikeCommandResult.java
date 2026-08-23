package com.planwith.planwith_fo_like.application.query;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeType;

public record LikeCommandResult(
		UUID memberUuid,
		LikeType likeType,
		UUID targetUuid,
		boolean liked,
		long likeCount,
		boolean alreadyApplied
) {
}
