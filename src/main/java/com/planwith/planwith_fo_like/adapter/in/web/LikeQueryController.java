package com.planwith.planwith_fo_like.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeCountResponse;
import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeStatusResponse;
import com.planwith.planwith_fo_like.application.port.in.GetMyLikeStatusQueryUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetTargetLikeCountQueryUseCase;
import com.planwith.planwith_fo_like.application.query.GetMyLikeStatusQuery;
import com.planwith.planwith_fo_like.application.query.GetTargetLikeCountQuery;
import com.planwith.planwith_fo_like.domain.model.LikeType;
import com.planwith.planwith_fo_like.domain.service.LikeCommonValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/likes")
@Tag(name = "like-query", description = "좋아요 Query API")
public class LikeQueryController {

	private final GetMyLikeStatusQueryUseCase getMyLikeStatusQueryUseCase;
	private final GetTargetLikeCountQueryUseCase getTargetLikeCountQueryUseCase;

	public LikeQueryController(
			GetMyLikeStatusQueryUseCase getMyLikeStatusQueryUseCase,
			GetTargetLikeCountQueryUseCase getTargetLikeCountQueryUseCase
	) {
		this.getMyLikeStatusQueryUseCase = getMyLikeStatusQueryUseCase;
		this.getTargetLikeCountQueryUseCase = getTargetLikeCountQueryUseCase;
	}

	// 내 좋아요 여부 조회
	@GetMapping("/{likeType}/{targetUuid}/me")
	@Operation(summary = "내 좋아요 여부 조회")
	public ResponseEntity<LikeStatusResponse> getMyLikeStatus(
			@RequestHeader("X-Member-UUID") UUID memberUuid,
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
}
