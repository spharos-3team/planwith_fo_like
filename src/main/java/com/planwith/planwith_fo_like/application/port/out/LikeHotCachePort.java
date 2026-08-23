package com.planwith.planwith_fo_like.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeType;

public interface LikeHotCachePort {

	boolean tryAcquireDuplicateGuard(UUID memberUuid, LikeType likeType, UUID targetUuid);

	void releaseDuplicateGuard(UUID memberUuid, LikeType likeType, UUID targetUuid);

	Optional<Boolean> findLiked(UUID memberUuid, LikeType likeType, UUID targetUuid);

	void markLiked(UUID memberUuid, LikeType likeType, UUID targetUuid);

	void markUnliked(UUID memberUuid, LikeType likeType, UUID targetUuid);

	Optional<Long> findCount(LikeType likeType, UUID targetUuid);

	void saveCount(LikeType likeType, UUID targetUuid, long likeCount);
}
