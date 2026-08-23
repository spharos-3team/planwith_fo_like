package com.planwith.planwith_fo_like.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_like.application.query.LikeCommandResult;
import com.planwith.planwith_fo_like.domain.model.LikeType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 명령 응답. Optimistic UI는 클릭 직후 화면 수를 ±1 하고, 성공 시 liked/likeCount로 확정하며 실패 시 이전 값으로 롤백한다.")
public record LikeCommandResponse(
		UUID memberUuid,
		LikeType likeType,
		UUID targetUuid,
		boolean liked,
		long likeCount,
		boolean alreadyApplied
) {

	public static LikeCommandResponse from(LikeCommandResult result) {
		return new LikeCommandResponse(
				result.memberUuid(),
				result.likeType(),
				result.targetUuid(),
				result.liked(),
				result.likeCount(),
				result.alreadyApplied()
		);
	}
}
