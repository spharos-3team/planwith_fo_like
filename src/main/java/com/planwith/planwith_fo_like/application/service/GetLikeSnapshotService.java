package com.planwith.planwith_fo_like.application.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_like.application.port.in.GetLikeSnapshotQueryUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetMyLikeStatusQueryUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetTargetLikeCountQueryUseCase;
import com.planwith.planwith_fo_like.application.query.GetLikeSnapshotQuery;
import com.planwith.planwith_fo_like.application.query.GetLikeSnapshotsQuery;
import com.planwith.planwith_fo_like.application.query.GetMyLikeStatusQuery;
import com.planwith.planwith_fo_like.application.query.GetTargetLikeCountQuery;
import com.planwith.planwith_fo_like.application.query.LikeSnapshotView;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;
import com.planwith.planwith_fo_like.domain.model.LikeType;
import com.planwith.planwith_fo_like.domain.service.LikeCommonValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GetLikeSnapshotService implements GetLikeSnapshotQueryUseCase {

	static final int MAX_SNAPSHOT_TARGETS = 50;

	private final GetMyLikeStatusQueryUseCase getMyLikeStatusQueryUseCase;
	private final GetTargetLikeCountQueryUseCase getTargetLikeCountQueryUseCase;

	public GetLikeSnapshotService(
			GetMyLikeStatusQueryUseCase getMyLikeStatusQueryUseCase,
			GetTargetLikeCountQueryUseCase getTargetLikeCountQueryUseCase
	) {
		this.getMyLikeStatusQueryUseCase = getMyLikeStatusQueryUseCase;
		this.getTargetLikeCountQueryUseCase = getTargetLikeCountQueryUseCase;
	}

	@Override
	@Transactional(readOnly = true)
	public LikeSnapshotView get(GetLikeSnapshotQuery query) {
		LikeCommonValidator.validate(query.likeType(), query.targetUuid());
		log.info(
				"GetLikeSnapshotService : get : 화면 좋아요 스냅샷 조회 - memberUuid={}, likeType={}, targetUuid={}",
				query.memberUuid(),
				query.likeType(),
				query.targetUuid()
		);
		return snapshot(query.memberUuid(), query.likeType(), query.targetUuid());
	}

	@Override
	@Transactional(readOnly = true)
	public List<LikeSnapshotView> getAll(GetLikeSnapshotsQuery query) {
		LikeCommonValidator.validateType(query.likeType());
		List<UUID> targetUuids = distinctTargets(query.targetUuids());
		log.info(
				"GetLikeSnapshotService : getAll : 화면 좋아요 스냅샷 일괄 조회 - memberUuid={}, likeType={}, targetCount={}",
				query.memberUuid(),
				query.likeType(),
				targetUuids.size()
		);
		List<LikeSnapshotView> snapshots = new ArrayList<>();
		for (UUID targetUuid : targetUuids) {
			snapshots.add(snapshot(query.memberUuid(), query.likeType(), targetUuid));
		}
		return snapshots;
	}

	private LikeSnapshotView snapshot(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		boolean liked = false;
		if (memberUuid != null) {
			liked = getMyLikeStatusQueryUseCase.get(new GetMyLikeStatusQuery(memberUuid, likeType, targetUuid)).liked();
		}
		long likeCount = getTargetLikeCountQueryUseCase.get(new GetTargetLikeCountQuery(likeType, targetUuid)).likeCount();
		return LikeSnapshotView.of(likeType, targetUuid, liked, likeCount);
	}

	private static List<UUID> distinctTargets(List<UUID> targetUuids) {
		if (targetUuids == null || targetUuids.isEmpty()) {
			throw new InvalidLikeTargetException("좋아요 대상 식별자가 없습니다.");
		}
		List<UUID> distinct = targetUuids.stream()
				.filter(targetUuid -> targetUuid != null)
				.collect(Collectors.toCollection(LinkedHashSet::new))
				.stream()
				.toList();
		if (distinct.isEmpty()) {
			throw new InvalidLikeTargetException("좋아요 대상 식별자가 없습니다.");
		}
		if (distinct.size() > MAX_SNAPSHOT_TARGETS) {
			throw new InvalidLikeTargetException("한 번에 조회할 수 있는 좋아요 대상 수를 초과했습니다.");
		}
		return distinct;
	}
}
