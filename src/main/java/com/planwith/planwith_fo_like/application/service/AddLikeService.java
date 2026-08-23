package com.planwith.planwith_fo_like.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeEventOutboxPort;
import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.application.port.out.LikeOutboxMessage;
import com.planwith.planwith_fo_like.application.port.out.LikeRelationPort;
import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.application.query.LikeCommandResult;
import com.planwith.planwith_fo_like.domain.event.LikeCreatedEvent;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.exception.DuplicateLikeException;
import com.planwith.planwith_fo_like.domain.model.LikeRelation;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.service.LikeTargetValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AddLikeService implements AddLikeUseCase {

	private static final String AGGREGATE_TYPE = "LIKE";

	private final LikeRelationPort likeRelationPort;
	private final LikeTargetCounterPort likeTargetCounterPort;
	private final LikeHotCachePort likeHotCachePort;
	private final AddLikeWriteService addLikeWriteService;
	private final Clock clock;

	public AddLikeService(
			LikeRelationPort likeRelationPort,
			LikeTargetCounterPort likeTargetCounterPort,
			LikeHotCachePort likeHotCachePort,
			AddLikeWriteService addLikeWriteService,
			Clock clock
	) {
		this.likeRelationPort = likeRelationPort;
		this.likeTargetCounterPort = likeTargetCounterPort;
		this.likeHotCachePort = likeHotCachePort;
		this.addLikeWriteService = addLikeWriteService;
		this.clock = clock;
	}

	@Override
	public LikeCommandResult add(AddLikeCommand command) {
		LikeTargetValidator.validate(command.targetType(), command.targetUuid(), command.targetOwnerUuid());
		log.info("AddLikeService : add : 좋아요 처리 시작 - memberUuid={}, targetType={}, targetUuid={}",
				command.memberUuid(), command.targetType(), command.targetUuid());
		likeHotCachePort.tryAcquireDuplicateGuard(command.memberUuid(), command.targetType(), command.targetUuid());
		try {
			return addLikeWriteService.write(command, clock.instant());
		} catch (DuplicateLikeException exception) {
			long likeCount = currentCount(command);
			refreshHotCache(command, likeCount);
			log.info("AddLikeService : add : 중복 좋아요 UNIQUE 멱등 처리 - memberUuid={}, targetUuid={}, likeCount={}",
					command.memberUuid(), command.targetUuid(), likeCount);
			return new LikeCommandResult(
					command.memberUuid(),
					command.targetType(),
					command.targetUuid(),
					true,
					likeCount,
					true
			);
		}
	}

	private long currentCount(AddLikeCommand command) {
		return likeTargetCounterPort.findByTarget(command.targetType(), command.targetUuid())
				.map(LikeTargetCounter::likeCount)
				.orElse(0L);
	}

	private void refreshHotCache(AddLikeCommand command, long likeCount) {
		likeHotCachePort.markLiked(command.memberUuid(), command.targetType(), command.targetUuid());
		likeHotCachePort.saveCount(command.targetType(), command.targetUuid(), likeCount);
		likeHotCachePort.releaseDuplicateGuard(command.memberUuid(), command.targetType(), command.targetUuid());
	}

	@Service
	static class AddLikeWriteService {

		private final LikeRelationPort likeRelationPort;
		private final LikeTargetCounterPort likeTargetCounterPort;
		private final LikeEventOutboxPort likeEventOutboxPort;
		private final LikeHotCachePort likeHotCachePort;
		private final LikeEventPayloadWriter payloadWriter;

		public AddLikeWriteService(
				LikeRelationPort likeRelationPort,
				LikeTargetCounterPort likeTargetCounterPort,
				LikeEventOutboxPort likeEventOutboxPort,
				LikeHotCachePort likeHotCachePort,
				LikeEventPayloadWriter payloadWriter
		) {
			this.likeRelationPort = likeRelationPort;
			this.likeTargetCounterPort = likeTargetCounterPort;
			this.likeEventOutboxPort = likeEventOutboxPort;
			this.likeHotCachePort = likeHotCachePort;
			this.payloadWriter = payloadWriter;
		}

		@Transactional
		LikeCommandResult write(AddLikeCommand command, Instant now) {
			LikeRelation inserted = likeRelationPort.insert(LikeRelation.create(
					command.memberUuid(),
					command.targetType(),
					command.targetUuid(),
					command.targetOwnerUuid(),
					now
			));
			LikeTargetCounter counter = likeTargetCounterPort.increment(command.targetType(), command.targetUuid());
			LikeCreatedEvent event = new LikeCreatedEvent(
					UUID.randomUUID(),
					command.targetType(),
					command.targetUuid(),
					command.targetOwnerUuid(),
					command.memberUuid(),
					now,
					counter.version()
			);
			likeEventOutboxPort.save(new LikeOutboxMessage(
					event.eventUuid(),
					AGGREGATE_TYPE,
					inserted.likeUuid(),
					LikeEventType.LIKE_CREATED.name(),
					payloadWriter.writeCreated(event),
					now
			));
			AfterCommitAction.run(() -> {
				likeHotCachePort.markLiked(command.memberUuid(), command.targetType(), command.targetUuid());
				likeHotCachePort.saveCount(command.targetType(), command.targetUuid(), counter.likeCount());
				likeHotCachePort.releaseDuplicateGuard(command.memberUuid(), command.targetType(), command.targetUuid());
			});
			log.info("AddLikeService : write : 좋아요 생성 완료 - memberUuid={}, likeUuid={}, likeCount={}, sourceVersion={}",
					command.memberUuid(), inserted.likeUuid(), counter.likeCount(), counter.version());
			return new LikeCommandResult(
					command.memberUuid(),
					command.targetType(),
					command.targetUuid(),
					true,
					counter.likeCount(),
					false
			);
		}
	}
}
