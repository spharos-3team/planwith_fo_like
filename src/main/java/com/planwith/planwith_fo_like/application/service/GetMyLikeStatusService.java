package com.planwith.planwith_fo_like.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.in.GetMyLikeStatusQueryUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.application.port.out.LikeManagementPort;
import com.planwith.planwith_fo_like.application.query.GetMyLikeStatusQuery;
import com.planwith.planwith_fo_like.application.query.LikeStatusView;
import com.planwith.planwith_fo_like.domain.model.LikeType;
import com.planwith.planwith_fo_like.domain.service.LikeCommonValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GetMyLikeStatusService implements GetMyLikeStatusQueryUseCase {

	private final LikeManagementPort likeManagementPort;
	private final LikeHotCachePort likeHotCachePort;

	public GetMyLikeStatusService(LikeManagementPort likeManagementPort, LikeHotCachePort likeHotCachePort) {
		this.likeManagementPort = likeManagementPort;
		this.likeHotCachePort = likeHotCachePort;
	}

	@Override
	@Transactional(readOnly = true)
	public LikeStatusView get(GetMyLikeStatusQuery query) {
		LikeCommonValidator.validate(query.memberUuid(), query.likeType(), query.targetUuid());
		log.debug("GetMyLikeStatusService : get : 내 좋아요 여부 조회 - memberUuid={}, likeType={}, targetUuid={}",
				query.memberUuid(), query.likeType(), query.targetUuid());
		boolean liked = findLikedFromCache(query.memberUuid(), query.likeType(), query.targetUuid())
				.orElseGet(() -> findActiveLikeFromDatabase(query));
		return new LikeStatusView(query.memberUuid(), query.likeType(), query.targetUuid(), liked);
	}

	private Optional<Boolean> findLikedFromCache(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		try {
			return likeHotCachePort.findLiked(memberUuid, likeType, targetUuid);
		} catch (RuntimeException exception) {
			log.warn("GetMyLikeStatusService : get : Redis 장애로 MySQL 조회로 전환 - memberUuid={}, targetUuid={}",
					memberUuid, targetUuid);
			return Optional.empty();
		}
	}

	private boolean findActiveLikeFromDatabase(GetMyLikeStatusQuery query) {
		log.info("GetMyLikeStatusService : get : Redis MISS로 like_management 활성 여부 조회 - memberUuid={}, likeType={}, targetUuid={}",
				query.memberUuid(), query.likeType(), query.targetUuid());
		boolean exists = likeManagementPort.existsActiveByMemberAndTarget(
				query.memberUuid(),
				query.likeType(),
				query.targetUuid()
		);
		if (exists) {
			likeHotCachePort.markLiked(query.memberUuid(), query.likeType(), query.targetUuid());
		}
		return exists;
	}
}
