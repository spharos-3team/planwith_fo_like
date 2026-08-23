package com.planwith.planwith_fo_like.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "좋아요 명령 요청")
public record LikeCommandRequest(
		@Schema(description = "대상 타입", example = "STORY")
		@NotBlank(message = "대상 타입은 필수입니다.")
		String targetType,

		@Schema(description = "대상 UUID")
		@NotNull(message = "대상 식별자는 필수입니다.")
		String targetUuid,

		@Schema(description = "대상 작성자 UUID")
		@NotNull(message = "대상 작성자 식별자는 필수입니다.")
		String targetOwnerUuid
) {
}
