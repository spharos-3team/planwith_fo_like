package com.planwith.planwith_fo_like.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.in.GetMyLikeStatusQueryUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.application.port.out.LikeRelationPort;
import com.planwith.planwith_fo_like.application.query.GetMyLikeStatusQuery;
import com.planwith.planwith_fo_like.application.query.LikeStatusView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GetMyLikeStatusService implements GetMyLikeStatusQueryUseCase {

	private final LikeRelationPort likeRelationPort;
	private final LikeHotCachePort likeHotCachePort;

	public GetMyLikeStatusService(LikeRelationPort likeRelationPort, LikeHotCachePort likeHotCachePort) {
		this.likeRelationPort = likeRelationPort;
		this.likeHotCachePort = likeHotCachePort;
	}

	@Override
	@Transactional(readOnly = true)
	public LikeStatusView get(GetMyLikeStatusQuery query) {
		log.debug("GetMyLikeStatusService : get : 내 좋아요 여부 조회 - memberUuid={}, targetType={}, targetUuid={}",
				query.memberUuid(), query.targetType(), query.targetUuid());
		boolean liked = likeHotCachePort.findLiked(query.memberUuid(), query.targetType(), query.targetUuid())
				.orElseGet(() -> {
					boolean exists = likeRelationPort.existsByMemberAndTarget(
							query.memberUuid(),
							query.targetType(),
							query.targetUuid()
					);
					if (exists) {
						likeHotCachePort.markLiked(query.memberUuid(), query.targetType(), query.targetUuid());
					}
					return exists;
				});
		return new LikeStatusView(query.memberUuid(), query.targetType(), query.targetUuid(), liked);
	}
}
