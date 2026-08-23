package com.planwith.planwith_fo_like.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_like.application.query.LikeSnapshotView;
import com.planwith.planwith_fo_like.domain.model.LikeType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토리/댓글 화면용 좋아요 스냅샷. Story/Comment DB 조인 없이 사용한다.")
public record LikeSnapshotResponse(
		LikeType likeType,
		UUID targetUuid,
		boolean liked,
		long likeCount,
		@Schema(description = "Optimistic UI 좋아요 시 즉시 표시할 수")
		long optimisticLikeCount,
		@Schema(description = "Optimistic UI 취소 시 즉시 표시할 수. 0 미만이 되지 않는다.")
		long optimisticUnlikeCount
) {

	public static LikeSnapshotResponse from(LikeSnapshotView view) {
		return new LikeSnapshotResponse(
				view.likeType(),
				view.targetUuid(),
				view.liked(),
				view.likeCount(),
				view.optimisticLikeCount(),
				view.optimisticUnlikeCount()
		);
	}
}
