package com.planwith.planwith_fo_like.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeCountResponse;
import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeStatusResponse;
import com.planwith.planwith_fo_like.application.port.in.GetMyLikeStatusQueryUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetTargetLikeCountQueryUseCase;
import com.planwith.planwith_fo_like.application.query.GetMyLikeStatusQuery;
import com.planwith.planwith_fo_like.application.query.GetTargetLikeCountQuery;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;
import com.planwith.planwith_fo_like.domain.model.LikeType;

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

	// 내가 해당 Target을 좋아요했는가
	@GetMapping("/me")
	@Operation(summary = "내 좋아요 여부 조회")
	public ResponseEntity<LikeStatusResponse> getMyLikeStatus(
			@RequestHeader("X-Member-UUID") UUID memberUuid,
			@RequestParam String targetType,
			@RequestParam UUID targetUuid
	) {
		log.info("LikeQueryController : GET getMyLikeStatus : 내 좋아요 여부 조회 요청 - memberUuid={}, targetUuid={}",
				memberUuid, targetUuid);
		LikeStatusResponse response = LikeStatusResponse.from(getMyLikeStatusQueryUseCase.get(
				new GetMyLikeStatusQuery(memberUuid, parseLikeType(targetType), targetUuid)
		));
		return ResponseEntity.ok(response);
	}

	// Target 좋아요 Count
	@GetMapping("/count")
	@Operation(summary = "대상 좋아요 수 조회")
	public ResponseEntity<LikeCountResponse> getTargetLikeCount(
			@RequestParam String targetType,
			@RequestParam UUID targetUuid
	) {
		log.info("LikeQueryController : GET getTargetLikeCount : 대상 좋아요 수 조회 요청 - targetUuid={}", targetUuid);
		LikeCountResponse response = LikeCountResponse.from(getTargetLikeCountQueryUseCase.get(
				new GetTargetLikeCountQuery(parseLikeType(targetType), targetUuid)
		));
		return ResponseEntity.ok(response);
	}

	private static LikeType parseLikeType(String rawType) {
		try {
			return LikeType.valueOf(rawType.trim().toUpperCase());
		} catch (RuntimeException exception) {
			throw new InvalidLikeTargetException("지원하지 않는 좋아요 대상 타입입니다.");
		}
	}
}
