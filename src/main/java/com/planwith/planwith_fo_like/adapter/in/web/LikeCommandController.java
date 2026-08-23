package com.planwith.planwith_fo_like.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeCommandResponse;
import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.command.RemoveLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.in.RemoveLikeUseCase;
import com.planwith.planwith_fo_like.domain.model.LikeType;
import com.planwith.planwith_fo_like.domain.service.LikeCommonValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/likes")
@Tag(name = "like", description = "좋아요 Command API")
public class LikeCommandController {

	private final AddLikeUseCase addLikeUseCase;
	private final RemoveLikeUseCase removeLikeUseCase;

	public LikeCommandController(AddLikeUseCase addLikeUseCase, RemoveLikeUseCase removeLikeUseCase) {
		this.addLikeUseCase = addLikeUseCase;
		this.removeLikeUseCase = removeLikeUseCase;
	}

	// 좋아요
	@PutMapping("/{likeType}/{targetUuid}")
	@Operation(summary = "좋아요", description = "프론트 Optimistic UI는 클릭 직후 수를 +1 하고, 이 API 실패 시 이전 값으로 롤백한다.")
	public ResponseEntity<LikeCommandResponse> addLike(
			@RequestHeader("X-Member-UUID") UUID memberUuid,
			@PathVariable String likeType,
			@PathVariable String targetUuid,
			@RequestParam(required = false) String targetOwnerUuid
	) {
		log.info("LikeCommandController : PUT addLike : 좋아요 요청 - memberUuid={}, likeType={}, targetUuid={}",
				memberUuid, likeType, targetUuid);
		LikeCommandResponse response = LikeCommandResponse.from(addLikeUseCase.add(toAddCommand(
				memberUuid,
				likeType,
				targetUuid,
				targetOwnerUuid
		)));
		log.info("LikeCommandController : PUT addLike : 좋아요 응답 - memberUuid={}, alreadyApplied={}",
				memberUuid, response.alreadyApplied());
		return ResponseEntity.ok(response);
	}

	// 좋아요 취소
	@DeleteMapping("/{likeType}/{targetUuid}")
	@Operation(summary = "좋아요 취소", description = "프론트 Optimistic UI는 클릭 직후 수를 -1 하고, 이 API 실패 시 이전 값으로 롤백한다.")
	public ResponseEntity<LikeCommandResponse> removeLike(
			@RequestHeader("X-Member-UUID") UUID memberUuid,
			@PathVariable String likeType,
			@PathVariable String targetUuid,
			@RequestParam(required = false) String targetOwnerUuid
	) {
		log.info("LikeCommandController : DELETE removeLike : 좋아요 취소 요청 - memberUuid={}, likeType={}, targetUuid={}",
				memberUuid, likeType, targetUuid);
		LikeCommandResponse response = LikeCommandResponse.from(removeLikeUseCase.remove(toRemoveCommand(
				memberUuid,
				likeType,
				targetUuid,
				targetOwnerUuid
		)));
		log.info("LikeCommandController : DELETE removeLike : 좋아요 취소 응답 - memberUuid={}, alreadyApplied={}",
				memberUuid, response.alreadyApplied());
		return ResponseEntity.ok(response);
	}

	private static AddLikeCommand toAddCommand(
			UUID memberUuid,
			String likeType,
			String targetUuid,
			String targetOwnerUuid
	) {
		return new AddLikeCommand(
				memberUuid,
				LikeType.from(likeType),
				LikeCommonValidator.parseUuid(targetUuid, "대상 식별자가 올바르지 않습니다."),
				LikeCommonValidator.parseOptionalUuid(targetOwnerUuid, "대상 작성자 식별자가 올바르지 않습니다.")
		);
	}

	private static RemoveLikeCommand toRemoveCommand(
			UUID memberUuid,
			String likeType,
			String targetUuid,
			String targetOwnerUuid
	) {
		return new RemoveLikeCommand(
				memberUuid,
				LikeType.from(likeType),
				LikeCommonValidator.parseUuid(targetUuid, "대상 식별자가 올바르지 않습니다."),
				LikeCommonValidator.parseOptionalUuid(targetOwnerUuid, "대상 작성자 식별자가 올바르지 않습니다.")
		);
	}
}
