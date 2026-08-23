package com.planwith.planwith_fo_like.adapter.out.persistence.outbox;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.out.LikeEventOutboxPort;
import com.planwith.planwith_fo_like.application.port.out.LikeOutboxMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LikeEventOutboxAdapter implements LikeEventOutboxPort {

	private final SpringDataLikeOutboxRepository repository;

	public LikeEventOutboxAdapter(SpringDataLikeOutboxRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public void save(LikeOutboxMessage message) {
		if (repository.existsByEventUuid(message.eventUuid())) {
			log.warn("LikeEventOutboxAdapter : save : 중복 Outbox 이벤트 저장 생략 - eventUuid={}",
					message.eventUuid());
			return;
		}
		repository.save(new LikeOutboxJpaEntity(
				message.eventUuid(),
				message.aggregateType(),
				message.aggregateUuid(),
				message.eventType(),
				message.payload(),
				message.occurredAt()
		));
		log.info("LikeEventOutboxAdapter : save : 좋아요 Outbox 저장 완료 - eventUuid={}, eventType={}",
				message.eventUuid(), message.eventType());
	}
}
