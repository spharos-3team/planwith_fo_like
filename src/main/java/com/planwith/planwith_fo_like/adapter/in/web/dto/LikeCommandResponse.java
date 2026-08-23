package com.planwith.planwith_fo_like.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_like.application.query.LikeCommandResult;
import com.planwith.planwith_fo_like.domain.model.TargetType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 명령 응답")
public record LikeCommandResponse(
		UUID memberUuid,
		TargetType targetType,
		UUID targetUuid,
		boolean liked,
		long likeCount,
		boolean alreadyApplied
) {

	public static LikeCommandResponse from(LikeCommandResult result) {
		return new LikeCommandResponse(
				result.memberUuid(),
				result.targetType(),
				result.targetUuid(),
				result.liked(),
				result.likeCount(),
				result.alreadyApplied()
		);
	}
}
