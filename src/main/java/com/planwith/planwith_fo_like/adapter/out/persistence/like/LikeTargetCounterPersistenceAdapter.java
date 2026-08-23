package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.model.TargetType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LikeTargetCounterPersistenceAdapter implements LikeTargetCounterPort {

	private final SpringDataLikeTargetCounterRepository repository;

	public LikeTargetCounterPersistenceAdapter(SpringDataLikeTargetCounterRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public LikeTargetCounter increment(TargetType targetType, UUID targetUuid) {
		LikeTargetCounterJpaEntity entity = lockOrCreate(targetType, targetUuid);
		entity.increment();
		LikeTargetCounterJpaEntity saved = repository.saveAndFlush(entity);
		log.debug("LikeTargetCounterPersistenceAdapter : increment : 좋아요 카운터 증가 - targetType={}, targetUuid={}, likeCount={}",
				targetType, targetUuid, saved.likeCount());
		return LikePersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional
	public LikeTargetCounter decrement(TargetType targetType, UUID targetUuid) {
		LikeTargetCounterJpaEntity entity = lockOrCreate(targetType, targetUuid);
		entity.decrement();
		LikeTargetCounterJpaEntity saved = repository.saveAndFlush(entity);
		log.debug("LikeTargetCounterPersistenceAdapter : decrement : 좋아요 카운터 감소 - targetType={}, targetUuid={}, likeCount={}",
				targetType, targetUuid, saved.likeCount());
		return LikePersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<LikeTargetCounter> findByTarget(TargetType targetType, UUID targetUuid) {
		return repository.findByTargetTypeAndTargetUuid(targetType, targetUuid)
				.map(LikePersistenceMapper::toDomain);
	}

	private LikeTargetCounterJpaEntity lockOrCreate(TargetType targetType, UUID targetUuid) {
		Optional<LikeTargetCounterJpaEntity> locked = repository.findByTargetForUpdate(targetType, targetUuid);
		if (locked.isPresent()) {
			return locked.get();
		}
		try {
			return repository.saveAndFlush(new LikeTargetCounterJpaEntity(targetType, targetUuid));
		} catch (DataIntegrityViolationException exception) {
			return repository.findByTargetForUpdate(targetType, targetUuid)
					.orElseThrow(() -> exception);
		}
	}
}
