package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.planwith.planwith_fo_like.domain.model.TargetType;

import jakarta.persistence.LockModeType;

public interface SpringDataLikeTargetCounterRepository extends JpaRepository<LikeTargetCounterJpaEntity, Long> {

	Optional<LikeTargetCounterJpaEntity> findByTargetTypeAndTargetUuid(TargetType targetType, UUID targetUuid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select counter
			from LikeTargetCounterJpaEntity counter
			where counter.targetType = :targetType
				and counter.targetUuid = :targetUuid
			""")
	Optional<LikeTargetCounterJpaEntity> findByTargetForUpdate(
			@Param("targetType") TargetType targetType,
			@Param("targetUuid") UUID targetUuid
	);
}
