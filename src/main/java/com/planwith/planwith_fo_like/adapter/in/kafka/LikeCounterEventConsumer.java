package com.planwith.planwith_fo_like.adapter.in.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_like.application.port.in.ApplyLikeCountUseCase;
import com.planwith.planwith_fo_like.application.service.LikeEventPayloadReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "like.kafka.consumer-enabled", havingValue = "true")
public class LikeCounterEventConsumer {

	private final LikeEventPayloadReader payloadReader;
	private final ApplyLikeCountUseCase applyLikeCountUseCase;

	public LikeCounterEventConsumer(
			LikeEventPayloadReader payloadReader,
			ApplyLikeCountUseCase applyLikeCountUseCase
	) {
		this.payloadReader = payloadReader;
		this.applyLikeCountUseCase = applyLikeCountUseCase;
	}

	@KafkaListener(
			topics = {
					"${like.kafka.topics.like-created}",
					"${like.kafka.topics.like-removed}"
			},
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void consume(String payload) {
		log.info("LikeCounterEventConsumer : consume : 좋아요 카운트 이벤트 수신");
		applyLikeCountUseCase.apply(payloadReader.read(payload));
	}
}
