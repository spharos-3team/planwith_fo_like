package com.planwith.planwith_fo_like.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.in.GetMyLikeStatusQueryUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.application.port.out.LikeManagementPort;
import com.planwith.planwith_fo_like.application.query.GetMyLikeStatusQuery;
import com.planwith.planwith_fo_like.application.query.LikeStatusView;

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
		log.debug("GetMyLikeStatusService : get : 내 좋아요 여부 조회 - memberUuid={}, likeType={}, targetUuid={}",
				query.memberUuid(), query.likeType(), query.targetUuid());
		boolean liked = likeHotCachePort.findLiked(query.memberUuid(), query.likeType(), query.targetUuid())
				.orElseGet(() -> {
					boolean exists = likeManagementPort.existsActiveByMemberAndTarget(
							query.memberUuid(),
							query.likeType(),
							query.targetUuid()
					);
					if (exists) {
						likeHotCachePort.markLiked(query.memberUuid(), query.likeType(), query.targetUuid());
					}
					return exists;
				});
		return new LikeStatusView(query.memberUuid(), query.likeType(), query.targetUuid(), liked);
	}
}
