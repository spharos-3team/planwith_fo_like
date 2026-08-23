package com.planwith.planwith_fo_like.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_like.application.port.out.LikeEventPublisher;

@Profile("test")
@Component
public class NoOpLikeEventPublisher implements LikeEventPublisher {

	@Override
	public CompletableFuture<Void> publish(String topic, String key, String payload) {
		return CompletableFuture.completedFuture(null);
	}
}
