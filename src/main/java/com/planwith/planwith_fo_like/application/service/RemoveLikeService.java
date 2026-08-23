package com.planwith.planwith_fo_like.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.command.RemoveLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.RemoveLikeUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeEventOutboxPort;
import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.application.port.out.LikeOutboxMessage;
import com.planwith.planwith_fo_like.application.port.out.LikeRelationPort;
import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.application.query.LikeCommandResult;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.event.LikeRemovedEvent;
import com.planwith.planwith_fo_like.domain.model.LikeRelation;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.service.LikeTargetValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RemoveLikeService implements RemoveLikeUseCase {

	private static final String AGGREGATE_TYPE = "LIKE";

	private final LikeRelationPort likeRelationPort;
	private final LikeTargetCounterPort likeTargetCounterPort;
	private final LikeEventOutboxPort likeEventOutboxPort;
	private final LikeHotCachePort likeHotCachePort;
	private final LikeEventPayloadWriter payloadWriter;
	private final Clock clock;

	public RemoveLikeService(
			LikeRelationPort likeRelationPort,
			LikeTargetCounterPort likeTargetCounterPort,
			LikeEventOutboxPort likeEventOutboxPort,
			LikeHotCachePort likeHotCachePort,
			LikeEventPayloadWriter payloadWriter,
			Clock clock
	) {
		this.likeRelationPort = likeRelationPort;
		this.likeTargetCounterPort = likeTargetCounterPort;
		this.likeEventOutboxPort = likeEventOutboxPort;
		this.likeHotCachePort = likeHotCachePort;
		this.payloadWriter = payloadWriter;
		this.clock = clock;
	}

	@Override
	@Transactional
	public LikeCommandResult remove(RemoveLikeCommand command) {
		LikeTargetValidator.validate(command.targetType(), command.targetUuid(), command.targetOwnerUuid());
		log.info("RemoveLikeService : remove : 좋아요 취소 처리 시작 - memberUuid={}, targetType={}, targetUuid={}",
				command.memberUuid(), command.targetType(), command.targetUuid());

		Optional<LikeRelation> deleted = likeRelationPort.deleteByMemberAndTarget(
				command.memberUuid(),
				command.targetType(),
				command.targetUuid()
		);
		if (deleted.isEmpty()) {
			long likeCount = currentCount(command);
			refreshHotCache(command, likeCount);
			log.info("RemoveLikeService : remove : 좋아요 없음 멱등 처리 - memberUuid={}, targetUuid={}, likeCount={}",
					command.memberUuid(), command.targetUuid(), likeCount);
			return new LikeCommandResult(
					command.memberUuid(),
					command.targetType(),
					command.targetUuid(),
					false,
					likeCount,
					true
			);
		}

		LikeTargetCounter counter = likeTargetCounterPort.decrement(command.targetType(), command.targetUuid());
		Instant now = clock.instant();
		LikeRemovedEvent event = new LikeRemovedEvent(
				UUID.randomUUID(),
				command.targetType(),
				command.targetUuid(),
				deleted.get().targetOwnerUuid(),
				command.memberUuid(),
				now,
				counter.version()
		);
		likeEventOutboxPort.save(new LikeOutboxMessage(
				event.eventUuid(),
				AGGREGATE_TYPE,
				deleted.get().likeUuid(),
				LikeEventType.LIKE_REMOVED.name(),
				payloadWriter.writeRemoved(event),
				now
		));
		refreshHotCache(command, counter.likeCount());
		log.info("RemoveLikeService : remove : 좋아요 취소 완료 - memberUuid={}, likeUuid={}, likeCount={}, sourceVersion={}",
				command.memberUuid(), deleted.get().likeUuid(), counter.likeCount(), counter.version());
		return new LikeCommandResult(
				command.memberUuid(),
				command.targetType(),
				command.targetUuid(),
				false,
				counter.likeCount(),
				false
		);
	}

	private long currentCount(RemoveLikeCommand command) {
		return likeTargetCounterPort.findByTarget(command.targetType(), command.targetUuid())
				.map(LikeTargetCounter::likeCount)
				.orElse(0L);
	}

	private void refreshHotCache(RemoveLikeCommand command, long likeCount) {
		AfterCommitAction.run(() -> {
			likeHotCachePort.markUnliked(command.memberUuid(), command.targetType(), command.targetUuid());
			likeHotCachePort.saveCount(command.targetType(), command.targetUuid(), likeCount);
			likeHotCachePort.releaseDuplicateGuard(command.memberUuid(), command.targetType(), command.targetUuid());
		});
	}
}
