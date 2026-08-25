package com.planwith.planwith_fo_like.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeCountResponse;
import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeSnapshotBatchRequest;
import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeSnapshotBatchResponse;
import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeSnapshotResponse;
import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeStatusResponse;
import com.planwith.planwith_fo_like.application.port.in.GetLikeSnapshotQueryUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetMyLikeStatusQueryUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetTargetLikeCountQueryUseCase;
import com.planwith.planwith_fo_like.application.query.GetLikeSnapshotQuery;
import com.planwith.planwith_fo_like.application.query.GetLikeSnapshotsQuery;
import com.planwith.planwith_fo_like.application.query.GetMyLikeStatusQuery;
import com.planwith.planwith_fo_like.application.query.GetTargetLikeCountQuery;
import com.planwith.planwith_fo_like.domain.model.LikeType;
import com.planwith.planwith_fo_like.domain.service.LikeCommonValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/likes")
@Tag(name = "like-query", description = "좋아요 Query API. Story/Comment는 Like 테이블을 조인하지 않고 이 API만 사용한다.")
public class LikeQueryController {

	private final GetMyLikeStatusQueryUseCase getMyLikeStatusQueryUseCase;
	private final GetTargetLikeCountQueryUseCase getTargetLikeCountQueryUseCase;
	private final GetLikeSnapshotQueryUseCase getLikeSnapshotQueryUseCase;

	public LikeQueryController(
			GetMyLikeStatusQueryUseCase getMyLikeStatusQueryUseCase,
			GetTargetLikeCountQueryUseCase getTargetLikeCountQueryUseCase,
			GetLikeSnapshotQueryUseCase getLikeSnapshotQueryUseCase
	) {
		this.getMyLikeStatusQueryUseCase = getMyLikeStatusQueryUseCase;
		this.getTargetLikeCountQueryUseCase = getTargetLikeCountQueryUseCase;
		this.getLikeSnapshotQueryUseCase = getLikeSnapshotQueryUseCase;
	}

	// 내 좋아요 여부 조회
	@GetMapping("/{likeType}/{targetUuid}/me")
	@Operation(summary = "내 좋아요 여부 조회")
	public ResponseEntity<LikeStatusResponse> getMyLikeStatus(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid,
			@PathVariable String likeType,
			@PathVariable String targetUuid
	) {
		log.info("LikeQueryController : GET getMyLikeStatus : 내 좋아요 여부 조회 요청 - memberUuid={}, likeType={}, targetUuid={}",
				memberUuid, likeType, targetUuid);
		LikeStatusResponse response = LikeStatusResponse.from(getMyLikeStatusQueryUseCase.get(
				new GetMyLikeStatusQuery(
						memberUuid,
						LikeType.from(likeType),
						LikeCommonValidator.parseUuid(targetUuid, "대상 식별자가 올바르지 않습니다.")
				)
		));
		return ResponseEntity.ok(response);
	}

	// 대상 좋아요 수 조회
	@GetMapping("/{likeType}/{targetUuid}/count")
	@Operation(summary = "대상 좋아요 수 조회")
	public ResponseEntity<LikeCountResponse> getTargetLikeCount(
			@PathVariable String likeType,
			@PathVariable String targetUuid
	) {
		log.info("LikeQueryController : GET getTargetLikeCount : 대상 좋아요 수 조회 요청 - likeType={}, targetUuid={}",
				likeType, targetUuid);
		LikeCountResponse response = LikeCountResponse.from(getTargetLikeCountQueryUseCase.get(
				new GetTargetLikeCountQuery(
						LikeType.from(likeType),
						LikeCommonValidator.parseUuid(targetUuid, "대상 식별자가 올바르지 않습니다.")
				)
		));
		return ResponseEntity.ok(response);
	}

	// 화면 좋아요 스냅샷 조회
	@GetMapping("/{likeType}/{targetUuid}")
	@Operation(
			summary = "화면 좋아요 스냅샷 조회",
			description = "스토리/댓글 화면 초기 렌더용 liked와 likeCount를 한 번에 반환한다. "
					+ "Story/Comment 서비스는 Like DB를 조회하지 않는다. "
					+ "프론트는 optimisticLikeCount/optimisticUnlikeCount로 클릭 직후 수를 즉시 표시하고, "
					+ "Command API 실패 시 likeCount로 롤백한다. X-Auth-User-Id가 없으면 liked=false이다."
	)
	public ResponseEntity<LikeSnapshotResponse> getLikeSnapshot(
			@RequestHeader(value = "X-Auth-User-Id", required = false) UUID memberUuid,
			@PathVariable String likeType,
			@PathVariable String targetUuid
	) {
		log.info("LikeQueryController : GET getLikeSnapshot : 화면 좋아요 스냅샷 조회 요청 - memberUuid={}, likeType={}, targetUuid={}",
				memberUuid, likeType, targetUuid);
		LikeSnapshotResponse response = LikeSnapshotResponse.from(getLikeSnapshotQueryUseCase.get(
				new GetLikeSnapshotQuery(
						memberUuid,
						LikeType.from(likeType),
						LikeCommonValidator.parseUuid(targetUuid, "대상 식별자가 올바르지 않습니다.")
				)
		));
		return ResponseEntity.ok(response);
	}

	// 화면 좋아요 스냅샷 일괄 조회
	@PostMapping("/snapshots")
	@Operation(
			summary = "화면 좋아요 스냅샷 일괄 조회",
			description = "댓글 목록처럼 여러 대상의 liked와 likeCount를 조회한다. "
					+ "한 번에 최대 50건이며 Story/Comment DB 조인은 하지 않는다."
	)
	public ResponseEntity<LikeSnapshotBatchResponse> getLikeSnapshots(
			@RequestHeader(value = "X-Auth-User-Id", required = false) UUID memberUuid,
			@Valid @RequestBody LikeSnapshotBatchRequest request
	) {
		log.info("LikeQueryController : POST getLikeSnapshots : 화면 좋아요 스냅샷 일괄 조회 요청 - memberUuid={}, likeType={}, targetCount={}",
				memberUuid, request.likeType(), request.targetUuids() == null ? 0 : request.targetUuids().size());
		LikeSnapshotBatchResponse response = LikeSnapshotBatchResponse.from(getLikeSnapshotQueryUseCase.getAll(
				new GetLikeSnapshotsQuery(
						memberUuid,
						LikeType.from(request.likeType()),
						toTargetUuids(request.targetUuids())
				)
		));
		return ResponseEntity.ok(response);
	}

	private static List<UUID> toTargetUuids(List<String> rawTargetUuids) {
		return rawTargetUuids.stream()
				.map(rawTargetUuid -> LikeCommonValidator.parseUuid(rawTargetUuid, "대상 식별자가 올바르지 않습니다."))
				.toList();
	}
}
