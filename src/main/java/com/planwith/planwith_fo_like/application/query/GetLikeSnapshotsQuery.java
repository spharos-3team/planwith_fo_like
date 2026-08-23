package com.planwith.planwith_fo_like.application.query;

import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeType;

public record GetLikeSnapshotsQuery(
		UUID memberUuid,
		LikeType likeType,
		List<UUID> targetUuids
) {
}
