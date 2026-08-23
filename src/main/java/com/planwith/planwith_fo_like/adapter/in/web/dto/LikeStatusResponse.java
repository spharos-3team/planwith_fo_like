package com.planwith.planwith_fo_like.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_like.application.query.LikeStatusView;
import com.planwith.planwith_fo_like.domain.model.LikeType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 좋아요 여부 응답")
public record LikeStatusResponse(
		UUID memberUuid,
		LikeType likeType,
		UUID targetUuid,
		boolean liked
) {

	public static LikeStatusResponse from(LikeStatusView view) {
		return new LikeStatusResponse(view.memberUuid(), view.likeType(), view.targetUuid(), view.liked());
	}
}
