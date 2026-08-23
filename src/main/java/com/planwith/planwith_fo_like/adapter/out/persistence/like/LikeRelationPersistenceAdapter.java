package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.out.LikeRelationPort;
import com.planwith.planwith_fo_like.domain.exception.DuplicateLikeException;
import com.planwith.planwith_fo_like.domain.model.LikeRelation;
import com.planwith.planwith_fo_like.domain.model.TargetType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LikeRelationPersistenceAdapter implements LikeRelationPort {

	private final SpringDataLikeManagementRepository repository;

	public LikeRelationPersistenceAdapter(SpringDataLikeManagementRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public LikeRelation insert(LikeRelation relation) {
		try {
			LikeManagementJpaEntity saved = repository.saveAndFlush(LikePersistenceMapper.toEntity(relation));
			return LikePersistenceMapper.toDomain(saved);
		} catch (DataIntegrityViolationException exception) {
			log.info(
					"LikeRelationPersistenceAdapter : insert : UNIQUE 제약으로 중복 좋아요 차단 - memberUuid={}, targetType={}, targetUuid={}",
					relation.memberUuid(),
					relation.targetType(),
					relation.targetUuid()
			);
			throw new DuplicateLikeException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<LikeRelation> findByMemberAndTarget(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		return repository.findByMemberUuidAndTargetTypeAndTargetUuid(memberUuid, targetType, targetUuid)
				.map(LikePersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByMemberAndTarget(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		return repository.existsByMemberUuidAndTargetTypeAndTargetUuid(memberUuid, targetType, targetUuid);
	}

	@Override
	@Transactional
	public Optional<LikeRelation> deleteByMemberAndTarget(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		return repository.findByMemberUuidAndTargetTypeAndTargetUuid(memberUuid, targetType, targetUuid)
				.map(entity -> {
					LikeRelation domain = LikePersistenceMapper.toDomain(entity);
					repository.delete(entity);
					repository.flush();
					return domain;
				});
	}
}
