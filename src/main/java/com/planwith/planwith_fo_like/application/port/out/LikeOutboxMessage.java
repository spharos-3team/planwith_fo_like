package com.planwith.planwith_fo_like.application.port.out;

import java.time.Instant;
import java.util.UUID;

public record LikeOutboxMessage(
		UUID eventUuid,
		String aggregateType,
		UUID aggregateUuid,
		String eventType,
		String payload,
		Instant occurredAt
) {
}
