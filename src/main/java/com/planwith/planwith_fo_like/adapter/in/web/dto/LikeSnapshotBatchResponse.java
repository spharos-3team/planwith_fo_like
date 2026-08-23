package com.planwith.planwith_fo_like.adapter.in.web.dto;

import java.util.List;

import com.planwith.planwith_fo_like.application.query.LikeSnapshotView;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토리/댓글 화면용 좋아요 스냅샷 목록")
public record LikeSnapshotBatchResponse(
		List<LikeSnapshotResponse> snapshots
) {

	public static LikeSnapshotBatchResponse from(List<LikeSnapshotView> views) {
		return new LikeSnapshotBatchResponse(views.stream().map(LikeSnapshotResponse::from).toList());
	}
}
