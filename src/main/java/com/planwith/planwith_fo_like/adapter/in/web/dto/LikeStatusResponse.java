package com.planwith.planwith_fo_like.adapter.in.web.dto;

import com.planwith.planwith_fo_like.application.query.LikeStatusView;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 좋아요 여부 응답")
public record LikeStatusResponse(
		boolean liked
) {

	public static LikeStatusResponse from(LikeStatusView view) {
		return new LikeStatusResponse(view.liked());
	}
}
