package com.planwith.planwith_fo_like.application.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_like.application.port.out.LikeEventOutboxPort;
import com.planwith.planwith_fo_like.application.port.out.LikeOutboxMessage;
import com.planwith.planwith_fo_like.domain.event.LikeEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LikeEventRecorder {

	private static final String AGGREGATE_TYPE = "LIKE";

	private final LikeEventOutboxPort likeEventOutboxPort;
	private final LikeEventPayloadWriter payloadWriter;

	public LikeEventRecorder(LikeEventOutboxPort likeEventOutboxPort, LikeEventPayloadWriter payloadWriter) {
		this.likeEventOutboxPort = likeEventOutboxPort;
		this.payloadWriter = payloadWriter;
	}

	public void record(LikeEvent event, UUID targetOwnerUuid, long sourceVersion) {
		likeEventOutboxPort.save(new LikeOutboxMessage(
				event.eventId(),
				AGGREGATE_TYPE,
				event.likeUuid(),
				event.eventType().name(),
				payloadWriter.write(event, targetOwnerUuid, sourceVersion),
				event.occurredAt()
		));
		log.info(
				"LikeEventRecorder : record : 좋아요 이벤트 기록 - eventType={}, likeType={}, likeUuid={}, targetUuid={}",
				event.eventType(),
				event.likeType(),
				event.likeUuid(),
				event.targetUuid()
		);
	}
}
