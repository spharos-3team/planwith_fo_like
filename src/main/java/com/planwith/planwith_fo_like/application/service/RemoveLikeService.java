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
import com.planwith.planwith_fo_like.application.port.out.LikeManagementPort;
import com.planwith.planwith_fo_like.application.port.out.LikeOutboxMessage;
import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.application.query.LikeCommandResult;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.event.LikeRemovedEvent;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.service.LikeTargetValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RemoveLikeService implements RemoveLikeUseCase {

	private static final String AGGREGATE_TYPE = "LIKE";

	private final LikeManagementPort likeManagementPort;
	private final LikeTargetCounterPort likeTargetCounterPort;
	private final LikeEventOutboxPort likeEventOutboxPort;
	private final LikeHotCachePort likeHotCachePort;
	private final LikeEventPayloadWriter payloadWriter;
	private final Clock clock;

	public RemoveLikeService(
			LikeManagementPort likeManagementPort,
			LikeTargetCounterPort likeTargetCounterPort,
			LikeEventOutboxPort likeEventOutboxPort,
			LikeHotCachePort likeHotCachePort,
			LikeEventPayloadWriter payloadWriter,
			Clock clock
	) {
		this.likeManagementPort = likeManagementPort;
		this.likeTargetCounterPort = likeTargetCounterPort;
		this.likeEventOutboxPort = likeEventOutboxPort;
		this.likeHotCachePort = likeHotCachePort;
		this.payloadWriter = payloadWriter;
		this.clock = clock;
	}

	@Override
	@Transactional
	public LikeCommandResult remove(RemoveLikeCommand command) {
		LikeTargetValidator.validate(command.memberUuid(), command.likeType(), command.targetUuid());
		log.info("RemoveLikeService : remove : 좋아요 취소 처리 시작 - memberUuid={}, likeType={}, targetUuid={}",
				command.memberUuid(), command.likeType(), command.targetUuid());

		Instant now = clock.instant();
		Optional<LikeManagement> deleted = likeManagementPort.markDeletedByMemberAndTarget(
				command.memberUuid(),
				command.likeType(),
				command.targetUuid(),
				now
		);
		if (deleted.isEmpty()) {
			long likeCount = currentCount(command);
			refreshHotCache(command, likeCount);
			log.info("RemoveLikeService : remove : 좋아요 없음 멱등 처리 - memberUuid={}, targetUuid={}, likeCount={}",
					command.memberUuid(), command.targetUuid(), likeCount);
			return new LikeCommandResult(
					command.memberUuid(),
					command.likeType(),
					command.targetUuid(),
					false,
					likeCount,
					true
			);
		}

		LikeTargetCounter counter = likeTargetCounterPort.decrement(command.likeType(), command.targetUuid(), now);
		LikeRemovedEvent event = new LikeRemovedEvent(
				UUID.randomUUID(),
				command.likeType(),
				command.targetUuid(),
				command.targetOwnerUuid(),
				command.memberUuid(),
				now,
				counter.likeCount()
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
		log.info("RemoveLikeService : remove : 좋아요 취소 완료 - memberUuid={}, likeUuid={}, likeType={}, likeCount={}",
				command.memberUuid(), deleted.get().likeUuid(), deleted.get().likeType(), counter.likeCount());
		return new LikeCommandResult(
				command.memberUuid(),
				command.likeType(),
				command.targetUuid(),
				false,
				counter.likeCount(),
				false
		);
	}

	private long currentCount(RemoveLikeCommand command) {
		return likeTargetCounterPort.findByTarget(command.likeType(), command.targetUuid())
				.map(LikeTargetCounter::likeCount)
				.orElse(0L);
	}

	private void refreshHotCache(RemoveLikeCommand command, long likeCount) {
		AfterCommitAction.run(() -> {
			likeHotCachePort.markUnliked(command.memberUuid(), command.likeType(), command.targetUuid());
			likeHotCachePort.saveCount(command.likeType(), command.targetUuid(), likeCount);
			likeHotCachePort.releaseDuplicateGuard(command.memberUuid(), command.likeType(), command.targetUuid());
		});
	}
}
