package com.planwith.planwith_fo_like.application.port.out;

import java.util.concurrent.CompletableFuture;

public interface LikeEventPublisher {

	CompletableFuture<Void> publish(String topic, String key, String payload);
}
