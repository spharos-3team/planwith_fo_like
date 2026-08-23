package com.planwith.planwith_fo_like.adapter.out.persistence.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.out.LikeEventPublisher;
import com.planwith.planwith_fo_like.config.LikeKafkaProperties;
import com.planwith.planwith_fo_like.config.LikeOutboxProperties;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "like.outbox.enabled", havingValue = "true")
public class LikeOutboxRelay {

	private final SpringDataLikeOutboxRepository repository;
	private final LikeEventPublisher publisher;
	private final LikeOutboxProperties outboxProperties;
	private final LikeKafkaProperties kafkaProperties;
	private final Clock clock;

	public LikeOutboxRelay(
			SpringDataLikeOutboxRepository repository,
			LikeEventPublisher publisher,
			LikeOutboxProperties outboxProperties,
			LikeKafkaProperties kafkaProperties,
			Clock clock
	) {
		this.repository = repository;
		this.publisher = publisher;
		this.outboxProperties = outboxProperties;
		this.kafkaProperties = kafkaProperties;
		this.clock = clock;
	}

	@Scheduled(
			fixedDelayString = "${like.outbox.relay-interval:5s}",
			initialDelayString = "${like.outbox.relay-initial-delay:5s}"
	)
	@Transactional
	public void relayUnpublishedEvents() {
		int batchSize = outboxProperties.getRelayBatchSize() > 0
				? outboxProperties.getRelayBatchSize()
				: 50;
		Instant now = clock.instant();
		List<LikeOutboxJpaEntity> unpublished = repository.findDueUnpublished(now, PageRequest.of(0, batchSize));
		for (LikeOutboxJpaEntity outbox : unpublished) {
			if (outbox.isDue(now)) {
				publish(outbox, now);
			}
		}
	}

	private void publish(LikeOutboxJpaEntity outbox, Instant now) {
		try {
			publisher.publish(topicFor(outbox.eventType()), outbox.aggregateUuid().toString(), outbox.payload())
					.get(sendTimeoutMillis(), TimeUnit.MILLISECONDS);
			outbox.markPublished(now);
			log.info("LikeOutboxRelay : publish : 좋아요 Outbox 발행 완료 - eventUuid={}, eventType={}",
					outbox.eventUuid(), outbox.eventType());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			recordFailure(outbox, now);
			log.warn("LikeOutboxRelay : publish : 좋아요 Outbox 발행 중단 - eventUuid={}, retryCount={}",
					outbox.eventUuid(), outbox.retryCount());
		} catch (Exception exception) {
			recordFailure(outbox, now);
			if (outboxProperties.retryLimitReached(outbox.retryCount())) {
				log.error(
						"LikeOutboxRelay : publish : 좋아요 Outbox 최대 재시도 이후에도 미발행 유지 - eventUuid={}, retryCount={}",
						outbox.eventUuid(),
						outbox.retryCount()
				);
			} else {
				log.warn("LikeOutboxRelay : publish : 좋아요 Outbox 발행 실패 - eventUuid={}, retryCount={}",
						outbox.eventUuid(), outbox.retryCount());
			}
		}
	}

	private void recordFailure(LikeOutboxJpaEntity outbox, Instant now) {
		int nextRetryCount = outbox.retryCount() + 1;
		outbox.recordPublishFailure(outboxProperties.nextRetryAt(now, nextRetryCount));
	}

	private String topicFor(String eventType) {
		if (LikeEventType.LIKE_REMOVED.name().equals(eventType)) {
			return kafkaProperties.getTopics().getLikeRemoved();
		}
		if (!LikeEventType.LIKE_CREATED.name().equals(eventType)) {
			log.warn("LikeOutboxRelay : topicFor : 알 수 없는 Outbox eventType이라 like.created로 발행 - eventType={}",
					eventType);
		}
		return kafkaProperties.getTopics().getLikeCreated();
	}

	private long sendTimeoutMillis() {
		Duration timeout = outboxProperties.getSendTimeout();
		if (timeout == null || timeout.isZero() || timeout.isNegative()) {
			return Duration.ofSeconds(10).toMillis();
		}
		return timeout.toMillis();
	}
}
