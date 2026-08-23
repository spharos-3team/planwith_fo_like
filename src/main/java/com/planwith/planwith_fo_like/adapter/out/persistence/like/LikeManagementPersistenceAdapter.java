package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.out.LikeManagementPort;
import com.planwith.planwith_fo_like.domain.exception.DuplicateLikeException;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LikeManagementPersistenceAdapter implements LikeManagementPort {

	private final SpringDataLikeManagementRepository repository;

	public LikeManagementPersistenceAdapter(SpringDataLikeManagementRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public LikeManagement insert(LikeManagement like) {
		try {
			LikeManagementJpaEntity saved = repository.saveAndFlush(LikePersistenceMapper.toEntity(like));
			return LikePersistenceMapper.toDomain(saved);
		} catch (DataIntegrityViolationException exception) {
			log.info(
					"LikeManagementPersistenceAdapter : insert : UNIQUE 제약으로 중복 좋아요 차단 - memberUuid={}, likeType={}, targetUuid={}",
					like.memberUuid(),
					like.likeType(),
					like.targetUuid()
			);
			throw new DuplicateLikeException();
		}
	}

	@Override
	@Transactional
	public LikeManagement restoreDeleted(LikeManagement like, Instant now) {
		LikeManagementJpaEntity entity = repository.findByMemberUuidAndLikeTypeAndTargetUuid(
				like.memberUuid(),
				like.likeType(),
				like.targetUuid()
		).orElseThrow(() -> new InvalidLikeTargetException("복원할 좋아요가 없습니다."));
		LikeManagement current = LikePersistenceMapper.toDomain(entity);
		if (current.isActive()) {
			log.info(
					"LikeManagementPersistenceAdapter : restoreDeleted : 활성 좋아요 UNIQUE 중복 - memberUuid={}, likeType={}, targetUuid={}",
					like.memberUuid(),
					like.likeType(),
					like.targetUuid()
			);
			throw new DuplicateLikeException();
		}
		current.restoreDeleted(now);
		entity.apply(current.updatedAt(), current.deletedAt());
		return LikePersistenceMapper.toDomain(repository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<LikeManagement> findByMemberAndTarget(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		return repository.findByMemberUuidAndLikeTypeAndTargetUuid(memberUuid, likeType, targetUuid)
				.map(LikePersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<LikeManagement> findActiveByMemberAndTarget(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		return repository.findByMemberUuidAndLikeTypeAndTargetUuidAndDeletedAtIsNull(memberUuid, likeType, targetUuid)
				.map(LikePersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsActiveByMemberAndTarget(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		return repository.existsByMemberUuidAndLikeTypeAndTargetUuidAndDeletedAtIsNull(memberUuid, likeType, targetUuid);
	}

	@Override
	@Transactional
	public Optional<LikeManagement> markDeletedByMemberAndTarget(
			UUID memberUuid,
			LikeType likeType,
			UUID targetUuid,
			Instant now
	) {
		return repository.findByMemberUuidAndLikeTypeAndTargetUuidAndDeletedAtIsNull(memberUuid, likeType, targetUuid)
				.map(entity -> {
					LikeManagement domain = LikePersistenceMapper.toDomain(entity);
					domain.markDeleted(now);
					entity.apply(domain.updatedAt(), domain.deletedAt());
					repository.saveAndFlush(entity);
					return domain;
				});
	}
}
