package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.model.LikeType;

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
	public LikeTargetCounter increment(LikeType likeType, UUID targetUuid, Instant now) {
		LikeTargetCounterJpaEntity entity = lockOrCreate(likeType, targetUuid, now);
		entity.increment(now);
		LikeTargetCounterJpaEntity saved = repository.saveAndFlush(entity);
		log.debug("LikeTargetCounterPersistenceAdapter : increment : 좋아요 카운터 증가 - likeType={}, targetUuid={}, likeCount={}",
				likeType, targetUuid, saved.likeCount());
		return LikePersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional
	public LikeTargetCounter decrement(LikeType likeType, UUID targetUuid, Instant now) {
		LikeTargetCounterJpaEntity entity = lockOrCreate(likeType, targetUuid, now);
		entity.decrement(now);
		LikeTargetCounterJpaEntity saved = repository.saveAndFlush(entity);
		log.debug("LikeTargetCounterPersistenceAdapter : decrement : 좋아요 카운터 감소 - likeType={}, targetUuid={}, likeCount={}",
				likeType, targetUuid, saved.likeCount());
		return LikePersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<LikeTargetCounter> findByTarget(LikeType likeType, UUID targetUuid) {
		return repository.findByLikeTypeAndTargetUuid(likeType, targetUuid)
				.map(LikePersistenceMapper::toDomain);
	}

	private LikeTargetCounterJpaEntity lockOrCreate(LikeType likeType, UUID targetUuid, Instant now) {
		Optional<LikeTargetCounterJpaEntity> locked = repository.findByTargetForUpdate(likeType, targetUuid);
		if (locked.isPresent()) {
			return locked.get();
		}
		try {
			return repository.saveAndFlush(new LikeTargetCounterJpaEntity(likeType, targetUuid, now));
		} catch (DataIntegrityViolationException exception) {
			return repository.findByTargetForUpdate(likeType, targetUuid)
					.orElseThrow(() -> exception);
		}
	}
}
