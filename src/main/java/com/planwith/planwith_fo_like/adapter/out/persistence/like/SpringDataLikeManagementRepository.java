package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_like.domain.model.TargetType;

public interface SpringDataLikeManagementRepository extends JpaRepository<LikeManagementJpaEntity, Long> {

	boolean existsByMemberUuidAndTargetTypeAndTargetUuid(UUID memberUuid, TargetType targetType, UUID targetUuid);

	Optional<LikeManagementJpaEntity> findByMemberUuidAndTargetTypeAndTargetUuid(
			UUID memberUuid,
			TargetType targetType,
			UUID targetUuid
	);
}
