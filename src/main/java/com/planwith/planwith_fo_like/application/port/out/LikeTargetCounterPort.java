package com.planwith.planwith_fo_like.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.model.TargetType;

public interface LikeTargetCounterPort {

	LikeTargetCounter increment(TargetType targetType, UUID targetUuid);

	LikeTargetCounter decrement(TargetType targetType, UUID targetUuid);

	Optional<LikeTargetCounter> findByTarget(TargetType targetType, UUID targetUuid);
}
