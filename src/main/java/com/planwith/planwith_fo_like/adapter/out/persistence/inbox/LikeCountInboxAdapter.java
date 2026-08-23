package com.planwith.planwith_fo_like.adapter.out.persistence.inbox;

import java.time.Clock;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.out.LikeCountInboxPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LikeCountInboxAdapter implements LikeCountInboxPort {

	private final SpringDataLikeCountInboxRepository repository;
	private final Clock clock;

	public LikeCountInboxAdapter(SpringDataLikeCountInboxRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	@Override
	@Transactional
	public boolean tryMarkProcessed(UUID eventId) {
		if (repository.existsById(eventId)) {
			return false;
		}
		try {
			repository.saveAndFlush(new LikeCountInboxJpaEntity(eventId, clock.instant()));
			return true;
		} catch (DataIntegrityViolationException exception) {
			log.info("LikeCountInboxAdapter : tryMarkProcessed : 중복 카운트 이벤트 차단 - eventId={}", eventId);
			return false;
		}
	}
}
