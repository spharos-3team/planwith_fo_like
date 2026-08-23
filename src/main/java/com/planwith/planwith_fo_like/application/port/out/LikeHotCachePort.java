package com.planwith.planwith_fo_like.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.TargetType;

public interface LikeHotCachePort {

	boolean tryAcquireDuplicateGuard(UUID memberUuid, TargetType targetType, UUID targetUuid);

	void releaseDuplicateGuard(UUID memberUuid, TargetType targetType, UUID targetUuid);

	Optional<Boolean> findLiked(UUID memberUuid, TargetType targetType, UUID targetUuid);

	void markLiked(UUID memberUuid, TargetType targetType, UUID targetUuid);

	void markUnliked(UUID memberUuid, TargetType targetType, UUID targetUuid);

	Optional<Long> findCount(TargetType targetType, UUID targetUuid);

	void saveCount(TargetType targetType, UUID targetUuid, long likeCount);
}
