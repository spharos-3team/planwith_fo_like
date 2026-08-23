package com.planwith.planwith_fo_like.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeEventOutboxPort;
import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.application.port.out.LikeManagementPort;
import com.planwith.planwith_fo_like.application.port.out.LikeOutboxMessage;
import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.application.query.LikeCommandResult;
import com.planwith.planwith_fo_like.domain.event.LikeCreatedEvent;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.exception.DuplicateLikeException;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeManagementStatus;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.service.LikeCommonValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AddLikeService implements AddLikeUseCase {

	private static final String AGGREGATE_TYPE = "LIKE";

	private final LikeTargetCounterPort likeTargetCounterPort;
	private final LikeHotCachePort likeHotCachePort;
	private final AddLikeWriteService addLikeWriteService;
	private final Clock clock;

	public AddLikeService(
			LikeTargetCounterPort likeTargetCounterPort,
			LikeHotCachePort likeHotCachePort,
			AddLikeWriteService addLikeWriteService,
			Clock clock
	) {
		this.likeTargetCounterPort = likeTargetCounterPort;
		this.likeHotCachePort = likeHotCachePort;
		this.addLikeWriteService = addLikeWriteService;
		this.clock = clock;
	}

	@Override
	public LikeCommandResult add(AddLikeCommand command) {
		LikeCommonValidator.validateCommand(
				command.memberUuid(),
				command.likeType(),
				command.targetUuid(),
				command.targetOwnerUuid()
		);
		log.info("AddLikeService : add : 좋아요 처리 시작 - memberUuid={}, likeType={}, targetUuid={}",
				command.memberUuid(), command.likeType(), command.targetUuid());
		likeHotCachePort.tryAcquireDuplicateGuard(command.memberUuid(), command.likeType(), command.targetUuid());
		try {
			return addLikeWriteService.write(command, clock.instant());
		} catch (DuplicateLikeException exception) {
			long likeCount = currentCount(command);
			refreshHotCache(command, likeCount);
			log.info("AddLikeService : add : 중복 좋아요 UNIQUE 멱등 처리 - memberUuid={}, targetUuid={}, likeCount={}",
					command.memberUuid(), command.targetUuid(), likeCount);
			return new LikeCommandResult(
					command.memberUuid(),
					command.likeType(),
					command.targetUuid(),
					true,
					likeCount,
					true
			);
		}
	}

	private long currentCount(AddLikeCommand command) {
		return likeTargetCounterPort.findByTarget(command.likeType(), command.targetUuid())
				.map(LikeTargetCounter::likeCount)
				.orElse(0L);
	}

	private void refreshHotCache(AddLikeCommand command, long likeCount) {
		likeHotCachePort.markLiked(command.memberUuid(), command.likeType(), command.targetUuid());
		likeHotCachePort.saveCount(command.likeType(), command.targetUuid(), likeCount);
		likeHotCachePort.releaseDuplicateGuard(command.memberUuid(), command.likeType(), command.targetUuid());
	}

	@Service
	static class AddLikeWriteService {

		private final LikeManagementPort likeManagementPort;
		private final LikeTargetCounterPort likeTargetCounterPort;
		private final LikeEventOutboxPort likeEventOutboxPort;
		private final LikeHotCachePort likeHotCachePort;
		private final LikeEventPayloadWriter payloadWriter;

		public AddLikeWriteService(
				LikeManagementPort likeManagementPort,
				LikeTargetCounterPort likeTargetCounterPort,
				LikeEventOutboxPort likeEventOutboxPort,
				LikeHotCachePort likeHotCachePort,
				LikeEventPayloadWriter payloadWriter
		) {
			this.likeManagementPort = likeManagementPort;
			this.likeTargetCounterPort = likeTargetCounterPort;
			this.likeEventOutboxPort = likeEventOutboxPort;
			this.likeHotCachePort = likeHotCachePort;
			this.payloadWriter = payloadWriter;
		}

		@Transactional
		LikeCommandResult write(AddLikeCommand command, Instant now) {
			Optional<LikeManagement> existing = likeManagementPort.findByMemberAndTarget(
					command.memberUuid(),
					command.likeType(),
					command.targetUuid()
			);
			LikeManagementStatus status = LikeCommonValidator.requireAddable(existing);
			log.info(
					"AddLikeService : write : 좋아요 상태 판정 - status={}, memberUuid={}, likeType={}, targetUuid={}",
					status,
					command.memberUuid(),
					command.likeType(),
					command.targetUuid()
			);
			LikeManagement saved = status.isReLike()
					? likeManagementPort.restoreDeleted(existing.orElseThrow(), now)
					: likeManagementPort.insert(LikeManagement.create(
							command.memberUuid(),
							command.targetUuid(),
							command.likeType(),
							now
					));
			LikeTargetCounter counter = likeTargetCounterPort.increment(command.likeType(), command.targetUuid(), now);
			LikeCreatedEvent event = new LikeCreatedEvent(
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
					saved.likeUuid(),
					LikeEventType.LIKE_CREATED.name(),
					payloadWriter.writeCreated(event),
					now
			));
			AfterCommitAction.run(() -> {
				likeHotCachePort.markLiked(command.memberUuid(), command.likeType(), command.targetUuid());
				likeHotCachePort.saveCount(command.likeType(), command.targetUuid(), counter.likeCount());
				likeHotCachePort.releaseDuplicateGuard(command.memberUuid(), command.likeType(), command.targetUuid());
			});
			log.info(
					"AddLikeService : write : 좋아요 처리 완료 - status={}, memberUuid={}, likeUuid={}, likeType={}, likeCount={}",
					status,
					command.memberUuid(),
					saved.likeUuid(),
					saved.likeType(),
					counter.likeCount()
			);
			return new LikeCommandResult(
					command.memberUuid(),
					command.likeType(),
					command.targetUuid(),
					true,
					counter.likeCount(),
					false
			);
		}
	}
}
