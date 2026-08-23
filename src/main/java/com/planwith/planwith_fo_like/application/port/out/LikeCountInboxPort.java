package com.planwith.planwith_fo_like.application.port.out;

import java.util.UUID;

public interface LikeCountInboxPort {

	boolean tryMarkProcessed(UUID eventId);
}
