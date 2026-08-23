package com.planwith.planwith_fo_like.adapter.in.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "댓글 목록 등 여러 대상의 좋아요 스냅샷 조회 요청")
public record LikeSnapshotBatchRequest(
		@Schema(description = "대상 타입", example = "COMMENT")
		@NotBlank(message = "대상 타입은 필수입니다.")
		String likeType,

		@Schema(description = "대상 UUID 목록")
		@NotEmpty(message = "대상 식별자는 필수입니다.")
		List<String> targetUuids
) {
}
