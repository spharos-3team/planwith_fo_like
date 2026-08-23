package com.planwith.planwith_fo_like.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_like.application.port.out.LikeEventPublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("!test")
@Component
public class KafkaLikeEventPublisher implements LikeEventPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaLikeEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public CompletableFuture<Void> publish(String topic, String key, String payload) {
		log.info("KafkaLikeEventPublisher : publish : 좋아요 이벤트 Kafka 발행 시작 - topic={}, key={}", topic, key);
		return kafkaTemplate.send(topic, key, payload)
				.thenAccept(result -> log.info(
						"KafkaLikeEventPublisher : publish : 좋아요 이벤트 Kafka 발행 완료 - topic={}, key={}",
						topic,
						key
				));
	}
}
