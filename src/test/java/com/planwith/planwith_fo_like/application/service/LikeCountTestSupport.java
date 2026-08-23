package com.planwith.planwith_fo_like.application.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.planwith.planwith_fo_like.application.port.in.ApplyLikeCountUseCase;

final class LikeCountTestSupport {

	private LikeCountTestSupport() {
	}

	static void applyOutboxEvents(
			JdbcTemplate jdbcTemplate,
			LikeEventPayloadReader payloadReader,
			ApplyLikeCountUseCase applyLikeCountUseCase
	) {
		List<String> payloads = jdbcTemplate.queryForList("select payload from like_outbox", String.class);
		for (String payload : payloads) {
			applyLikeCountUseCase.apply(payloadReader.read(payload));
		}
	}
}
