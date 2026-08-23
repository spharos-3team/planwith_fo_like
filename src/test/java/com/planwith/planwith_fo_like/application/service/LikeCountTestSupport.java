package com.planwith.planwith_fo_like.application.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.planwith.planwith_fo_like.application.port.in.ApplyLikeCountUseCase;

public final class LikeCountTestSupport {

	private LikeCountTestSupport() {
	}

	public static void applyOutboxEvents(
			JdbcTemplate jdbcTemplate,
			LikeEventPayloadReader payloadReader,
			ApplyLikeCountUseCase applyLikeCountUseCase
	) {
		applyOutboxEventsContaining(jdbcTemplate, payloadReader, applyLikeCountUseCase, "");
	}

	public static int applyOutboxEventsContaining(
			JdbcTemplate jdbcTemplate,
			LikeEventPayloadReader payloadReader,
			ApplyLikeCountUseCase applyLikeCountUseCase,
			String token
	) {
		List<String> payloads = token == null || token.isBlank()
				? jdbcTemplate.queryForList("select payload from like_outbox", String.class)
				: jdbcTemplate.queryForList(
						"select payload from like_outbox where payload like ? order by occurred_at asc, outbox_id asc",
						String.class,
						"%" + token + "%"
				);
		for (String payload : payloads) {
			applyLikeCountUseCase.apply(payloadReader.read(payload));
		}
		return payloads.size();
	}
}
