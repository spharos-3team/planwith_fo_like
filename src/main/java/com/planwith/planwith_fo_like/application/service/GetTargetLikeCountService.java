package com.planwith.planwith_fo_like.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.in.GetTargetLikeCountQueryUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.application.query.GetTargetLikeCountQuery;
import com.planwith.planwith_fo_like.application.query.LikeCountView;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.model.LikeType;
import com.planwith.planwith_fo_like.domain.service.LikeCommonValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GetTargetLikeCountService implements GetTargetLikeCountQueryUseCase {

	private final LikeTargetCounterPort likeTargetCounterPort;
	private final LikeHotCachePort likeHotCachePort;

	public GetTargetLikeCountService(
			LikeTargetCounterPort likeTargetCounterPort,
			LikeHotCachePort likeHotCachePort
	) {
		this.likeTargetCounterPort = likeTargetCounterPort;
		this.likeHotCachePort = likeHotCachePort;
	}

	@Override
	@Transactional(readOnly = true)
	public LikeCountView get(GetTargetLikeCountQuery query) {
		LikeCommonValidator.validate(query.likeType(), query.targetUuid());
		log.debug("GetTargetLikeCountService : get : 대상 좋아요 수 조회 - likeType={}, targetUuid={}",
				query.likeType(), query.targetUuid());
		long likeCount = findCountFromCache(query.likeType(), query.targetUuid())
				.orElseGet(() -> findCountFromCounter(query));
		return new LikeCountView(query.likeType(), query.targetUuid(), likeCount);
	}

	private Optional<Long> findCountFromCache(LikeType likeType, UUID targetUuid) {
		try {
			return likeHotCachePort.findCount(likeType, targetUuid);
		} catch (RuntimeException exception) {
			log.warn("GetTargetLikeCountService : get : Redis 장애로 like_target_counter 조회로 전환 - likeType={}, targetUuid={}",
					likeType, targetUuid);
			return Optional.empty();
		}
	}

	private long findCountFromCounter(GetTargetLikeCountQuery query) {
		log.info("GetTargetLikeCountService : get : Redis MISS로 like_target_counter 조회 - likeType={}, targetUuid={}",
				query.likeType(), query.targetUuid());
		long count = likeTargetCounterPort.findByTarget(query.likeType(), query.targetUuid())
				.map(LikeTargetCounter::likeCount)
				.orElse(0L);
		likeHotCachePort.saveCount(query.likeType(), query.targetUuid(), count);
		return count;
	}
}
