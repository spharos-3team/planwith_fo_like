package com.planwith.planwith_fo_like.application.query;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeType;

public record GetLikeSnapshotQuery(
		UUID memberUuid,
		LikeType likeType,
		UUID targetUuid
) {
}
