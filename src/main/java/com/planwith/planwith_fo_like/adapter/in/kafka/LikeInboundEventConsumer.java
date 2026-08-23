package com.planwith.planwith_fo_like.adapter.in.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "like.kafka.consumer-enabled", havingValue = "true")
public class LikeInboundEventConsumer {

	public LikeInboundEventConsumer() {
		log.info("LikeInboundEventConsumer : init : inbound Kafka 소비 활성화됨");
	}
}
