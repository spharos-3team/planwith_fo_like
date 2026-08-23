package com.planwith.planwith_fo_like.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeType;

public interface LikeManagementPort {

	LikeManagement insert(LikeManagement like);

	Optional<LikeManagement> findActiveByMemberAndTarget(UUID memberUuid, LikeType likeType, UUID targetUuid);

	boolean existsActiveByMemberAndTarget(UUID memberUuid, LikeType likeType, UUID targetUuid);

	Optional<LikeManagement> markDeletedByMemberAndTarget(
			UUID memberUuid,
			LikeType likeType,
			UUID targetUuid,
			Instant now
	);
}
