package com.planwith.planwith_fo_like.application.query;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.TargetType;

public record LikeStatusView(
		UUID memberUuid,
		TargetType targetType,
		UUID targetUuid,
		boolean liked
) {
}
