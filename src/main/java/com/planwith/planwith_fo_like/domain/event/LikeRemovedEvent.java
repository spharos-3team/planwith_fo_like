package com.planwith.planwith_fo_like.domain.event;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeType;

public record LikeRemovedEvent(
		UUID eventUuid,
		LikeType likeType,
		UUID targetUuid,
		UUID targetOwnerUuid,
		UUID likerUuid,
		Instant occurredAt,
		long sourceVersion
) {
}
