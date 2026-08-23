package com.planwith.planwith_fo_like.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.model.LikeType;

public interface LikeTargetCounterPort {

	LikeTargetCounter increment(LikeType likeType, UUID targetUuid, Instant now);

	LikeTargetCounter decrement(LikeType likeType, UUID targetUuid, Instant now);

	Optional<LikeTargetCounter> findByTarget(LikeType likeType, UUID targetUuid);
}
