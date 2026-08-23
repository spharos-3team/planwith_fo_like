package com.planwith.planwith_fo_like.application.command;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeType;

public record AddLikeCommand(
		UUID memberUuid,
		LikeType likeType,
		UUID targetUuid,
		UUID targetOwnerUuid
) {
}
