package com.planwith.planwith_fo_like.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeCommandRequest;
import com.planwith.planwith_fo_like.adapter.in.web.dto.LikeCommandResponse;
import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.command.RemoveLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.in.RemoveLikeUseCase;
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
@Tag(name = "like", description = "좋아요 Command API")
public class LikeCommandController {

	private final AddLikeUseCase addLikeUseCase;
	private final RemoveLikeUseCase removeLikeUseCase;

	public LikeCommandController(AddLikeUseCase addLikeUseCase, RemoveLikeUseCase removeLikeUseCase) {
		this.addLikeUseCase = addLikeUseCase;
		this.removeLikeUseCase = removeLikeUseCase;
	}

	// 좋아요
	@PostMapping
	@Operation(summary = "좋아요")
	public ResponseEntity<LikeCommandResponse> addLike(
			@RequestHeader("X-Member-UUID") UUID memberUuid,
			@Valid @RequestBody LikeCommandRequest request
	) {
		log.info("LikeCommandController : POST addLike : 좋아요 요청 - memberUuid={}, targetUuid={}",
				memberUuid, request.targetUuid());
		LikeCommandResponse response = LikeCommandResponse.from(addLikeUseCase.add(new AddLikeCommand(
				memberUuid,
				LikeType.from(request.targetType()),
				LikeCommonValidator.parseUuid(request.targetUuid(), "대상 식별자가 올바르지 않습니다."),
				LikeCommonValidator.parseUuid(request.targetOwnerUuid(), "대상 작성자 식별자가 올바르지 않습니다.")
		)));
		log.info("LikeCommandController : POST addLike : 좋아요 응답 - memberUuid={}, alreadyApplied={}",
				memberUuid, response.alreadyApplied());
		return ResponseEntity.ok(response);
	}

	// 좋아요 취소
	@DeleteMapping
	@Operation(summary = "좋아요 취소")
	public ResponseEntity<LikeCommandResponse> removeLike(
			@RequestHeader("X-Member-UUID") UUID memberUuid,
			@Valid @RequestBody LikeCommandRequest request
	) {
		log.info("LikeCommandController : DELETE removeLike : 좋아요 취소 요청 - memberUuid={}, targetUuid={}",
				memberUuid, request.targetUuid());
		LikeCommandResponse response = LikeCommandResponse.from(removeLikeUseCase.remove(new RemoveLikeCommand(
				memberUuid,
				LikeType.from(request.targetType()),
				LikeCommonValidator.parseUuid(request.targetUuid(), "대상 식별자가 올바르지 않습니다."),
				LikeCommonValidator.parseUuid(request.targetOwnerUuid(), "대상 작성자 식별자가 올바르지 않습니다.")
		)));
		log.info("LikeCommandController : DELETE removeLike : 좋아요 취소 응답 - memberUuid={}, alreadyApplied={}",
				memberUuid, response.alreadyApplied());
		return ResponseEntity.ok(response);
	}

}
