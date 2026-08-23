package com.planwith.planwith_fo_like.application.command;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.TargetType;

public record AddLikeCommand(
		UUID memberUuid,
		TargetType targetType,
		UUID targetUuid,
		UUID targetOwnerUuid
) {
}
