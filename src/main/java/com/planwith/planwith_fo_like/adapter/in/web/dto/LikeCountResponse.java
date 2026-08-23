package com.planwith.planwith_fo_like.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_like.application.query.LikeCountView;
import com.planwith.planwith_fo_like.domain.model.TargetType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대상 좋아요 수 응답")
public record LikeCountResponse(
		TargetType targetType,
		UUID targetUuid,
		long likeCount
) {

	public static LikeCountResponse from(LikeCountView view) {
		return new LikeCountResponse(view.targetType(), view.targetUuid(), view.likeCount());
	}
}
