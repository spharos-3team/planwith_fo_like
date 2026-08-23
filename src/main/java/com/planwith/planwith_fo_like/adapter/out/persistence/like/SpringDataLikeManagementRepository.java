package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_like.domain.model.LikeType;

public interface SpringDataLikeManagementRepository extends JpaRepository<LikeManagementJpaEntity, Long> {

	Optional<LikeManagementJpaEntity> findByMemberUuidAndLikeTypeAndTargetUuid(
			UUID memberUuid,
			LikeType likeType,
			UUID targetUuid
	);

	Optional<LikeManagementJpaEntity> findByMemberUuidAndLikeTypeAndTargetUuidAndDeletedAtIsNull(
			UUID memberUuid,
			LikeType likeType,
			UUID targetUuid
	);

	boolean existsByMemberUuidAndLikeTypeAndTargetUuidAndDeletedAtIsNull(
			UUID memberUuid,
			LikeType likeType,
			UUID targetUuid
	);
}
