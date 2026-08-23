package com.planwith.planwith_fo_like.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeRelation;
import com.planwith.planwith_fo_like.domain.model.TargetType;

public interface LikeRelationPort {

	LikeRelation insert(LikeRelation relation);

	Optional<LikeRelation> findByMemberAndTarget(UUID memberUuid, TargetType targetType, UUID targetUuid);

	boolean existsByMemberAndTarget(UUID memberUuid, TargetType targetType, UUID targetUuid);

	Optional<LikeRelation> deleteByMemberAndTarget(UUID memberUuid, TargetType targetType, UUID targetUuid);
}
