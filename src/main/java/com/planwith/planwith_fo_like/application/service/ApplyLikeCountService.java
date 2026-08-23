package com.planwith.planwith_fo_like.application.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.command.LikeCountApplyCommand;
import com.planwith.planwith_fo_like.application.port.in.ApplyLikeCountUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeCountInboxPort;
import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.service.LikeCommonValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApplyLikeCountService implements ApplyLikeCountUseCase {

	private final LikeCountInboxPort likeCountInboxPort;
	private final LikeTargetCounterPort likeTargetCounterPort;
	private final LikeHotCachePort likeHotCachePort;
	private final Clock clock;

	public ApplyLikeCountService(
			LikeCountInboxPort likeCountInboxPort,
			LikeTargetCounterPort likeTargetCounterPort,
			LikeHotCachePort likeHotCachePort,
			Clock clock
	) {
		this.likeCountInboxPort = likeCountInboxPort;
		this.likeTargetCounterPort = likeTargetCounterPort;
		this.likeHotCachePort = likeHotCachePort;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void apply(LikeCountApplyCommand command) {
		LikeCommonValidator.validate(command.likeType(), command.targetUuid());
		if (command.eventId() == null || command.eventType() == null) {
			log.warn("ApplyLikeCountService : apply : 카운트 이벤트 식별값이 없어 건너뜀");
			return;
		}
		if (!likeCountInboxPort.tryMarkProcessed(command.eventId())) {
			log.info(
					"ApplyLikeCountService : apply : 이미 처리한 카운트 이벤트 건너뜀 - eventId={}, eventType={}, targetUuid={}",
					command.eventId(),
					command.eventType(),
					command.targetUuid()
			);
			return;
		}
		LikeTargetCounter counter = command.eventType().isUnlike()
				? likeTargetCounterPort.decrement(command.likeType(), command.targetUuid(), clock.instant())
				: likeTargetCounterPort.increment(command.likeType(), command.targetUuid(), clock.instant());
		likeHotCachePort.saveCount(command.likeType(), command.targetUuid(), counter.likeCount());
		log.info(
				"ApplyLikeCountService : apply : like_target_counter 반영 완료 - eventType={}, likeType={}, targetUuid={}, likeCount={}",
				command.eventType(),
				command.likeType(),
				command.targetUuid(),
				counter.likeCount()
		);
	}
}
