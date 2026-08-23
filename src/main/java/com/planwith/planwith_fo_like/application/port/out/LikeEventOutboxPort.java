package com.planwith.planwith_fo_like.application.port.out;

public interface LikeEventOutboxPort {

	void save(LikeOutboxMessage message);
}
