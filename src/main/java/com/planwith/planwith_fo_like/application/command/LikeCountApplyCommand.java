package com.planwith.planwith_fo_like.application.command;

import java.util.UUID;

import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.model.LikeType;

public record LikeCountApplyCommand(
		UUID eventId,
		LikeEventType eventType,
		LikeType likeType,
		UUID targetUuid
) {
}
