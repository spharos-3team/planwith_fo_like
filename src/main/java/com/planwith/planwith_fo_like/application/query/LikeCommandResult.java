package com.planwith.planwith_fo_like.application.query;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.TargetType;

public record LikeCommandResult(
		UUID memberUuid,
		TargetType targetType,
		UUID targetUuid,
		boolean liked,
		long likeCount,
		boolean alreadyApplied
) {
}
