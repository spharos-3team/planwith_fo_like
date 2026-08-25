package com.planwith.planwith_fo_like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.planwith.planwith_fo_like.adapter.out.redis.InMemoryLikeHotCacheAdapter;
import com.planwith.planwith_fo_like.application.command.LikeCountApplyCommand;
import com.planwith.planwith_fo_like.application.port.in.ApplyLikeCountUseCase;
import com.planwith.planwith_fo_like.application.service.LikeCountTestSupport;
import com.planwith.planwith_fo_like.application.service.LikeEventPayloadReader;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.model.LikeType;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class LikeEndToEndConsistencyIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ApplyLikeCountUseCase applyLikeCountUseCase;

	@Autowired
	private LikeEventPayloadReader payloadReader;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private InMemoryLikeHotCacheAdapter likeHotCacheAdapter;

	@Test
	void storyLikeUnlikeRelikeKeepsFullEventFlowConsistent() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		String storyPath = likePath(LikeType.STORY, targetUuid);

		mockMvc.perform(put(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.alreadyApplied").value(false));

		assertThat(activeLikeCount(memberUuid, LikeType.STORY, targetUuid)).isEqualTo(1);
		assertThat(outboxEventCount(targetUuid, LikeEventType.LIKE)).isEqualTo(1);
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isNull();

		mockMvc.perform(put(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.alreadyApplied").value(true));
		assertThat(outboxEventCount(targetUuid, LikeEventType.LIKE)).isEqualTo(1);
		assertThat(likeManagementRowCount(memberUuid, LikeType.STORY, targetUuid)).isEqualTo(1);

		mockMvc.perform(get(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.optimisticLikeCount").value(2))
				.andExpect(jsonPath("$.optimisticUnlikeCount").value(0));
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isNull();

		int firstApply = applyEvents(targetUuid);
		assertThat(firstApply).isEqualTo(1);
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isEqualTo(1L);
		assertThat(applyEvents(targetUuid)).isEqualTo(1);
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isEqualTo(1L);
		assertThat(inboxCount()).isGreaterThanOrEqualTo(1);

		mockMvc.perform(delete(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0))
				.andExpect(jsonPath("$.alreadyApplied").value(false));
		assertThat(activeLikeCount(memberUuid, LikeType.STORY, targetUuid)).isZero();
		assertThat(outboxEventCount(targetUuid, LikeEventType.UNLIKE)).isEqualTo(1);

		applyEvents(targetUuid);
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isZero();

		mockMvc.perform(delete(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.alreadyApplied").value(true));
		assertThat(outboxEventCount(targetUuid, LikeEventType.UNLIKE)).isEqualTo(1);

		applyLikeCountUseCase.apply(new LikeCountApplyCommand(
				UUID.randomUUID(),
				LikeEventType.UNLIKE,
				LikeType.STORY,
				targetUuid
		));
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isZero();

		mockMvc.perform(put(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.alreadyApplied").value(false));
		assertThat(likeManagementRowCount(memberUuid, LikeType.STORY, targetUuid)).isEqualTo(1);
		assertThat(activeLikeCount(memberUuid, LikeType.STORY, targetUuid)).isEqualTo(1);

		applyEvents(targetUuid);
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isEqualTo(1L);
	}

	@Test
	void commentFlowAndSameUuidAreSeparatedByLikeType() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		String storyPath = likePath(LikeType.STORY, targetUuid);
		String commentPath = likePath(LikeType.COMMENT, targetUuid);

		mockMvc.perform(put(commentPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeType").value("COMMENT"))
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1));

		mockMvc.perform(put(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeType").value("STORY"))
				.andExpect(jsonPath("$.likeCount").value(1));

		applyEvents(targetUuid);
		assertThat(dbCounter(LikeType.COMMENT, targetUuid)).isEqualTo(1L);
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isEqualTo(1L);

		mockMvc.perform(delete(commentPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0));

		applyEvents(targetUuid);
		assertThat(dbCounter(LikeType.COMMENT, targetUuid)).isZero();
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isEqualTo(1L);
		assertThat(activeLikeCount(memberUuid, LikeType.COMMENT, targetUuid)).isZero();
		assertThat(activeLikeCount(memberUuid, LikeType.STORY, targetUuid)).isEqualTo(1);
	}

	@Test
	void failedCommandLeavesStateForOptimisticUiRollback() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		String storyPath = likePath(LikeType.STORY, targetUuid);

		mockMvc.perform(get(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0))
				.andExpect(jsonPath("$.optimisticLikeCount").value(1));

		mockMvc.perform(put(storyPath))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
		mockMvc.perform(put("/api/v1/likes/PLAN/" + targetUuid).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_LIKE_TYPE"));

		mockMvc.perform(get(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0));
		assertThat(likeManagementRowCount(memberUuid, LikeType.STORY, targetUuid)).isZero();
		assertThat(outboxEventCount(targetUuid, LikeEventType.LIKE)).isZero();
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isNull();

		mockMvc.perform(put(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1));

		mockMvc.perform(delete(storyPath))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(delete("/api/v1/likes/PLAN/" + targetUuid).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1));
		assertThat(activeLikeCount(memberUuid, LikeType.STORY, targetUuid)).isEqualTo(1);
	}

	@Test
	void redisMissAfterCounterApplyFallsBackToMysql() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		String storyPath = likePath(LikeType.STORY, targetUuid);

		mockMvc.perform(put(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk());
		applyEvents(targetUuid);
		likeHotCacheAdapter.clear();

		mockMvc.perform(get(storyPath).header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1));
		mockMvc.perform(get(storyPath + "/me").header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true));
		mockMvc.perform(get(storyPath + "/count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeCount").value(1));
		assertThat(dbCounter(LikeType.STORY, targetUuid)).isEqualTo(1L);
	}

	private int applyEvents(UUID targetUuid) {
		return LikeCountTestSupport.applyOutboxEventsContaining(
				jdbcTemplate,
				payloadReader,
				applyLikeCountUseCase,
				targetUuid.toString()
		);
	}

	private static String likePath(LikeType likeType, UUID targetUuid) {
		return "/api/v1/likes/" + likeType.name() + "/" + targetUuid;
	}

	private Integer activeLikeCount(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		return jdbcTemplate.queryForObject(
				"""
						select count(*)
						from like_management
						where member_uuid = ? and like_type = ? and target_uuid = ? and deleted_at is null
						""",
				Integer.class,
				memberUuid.toString(),
				likeType.name(),
				targetUuid.toString()
		);
	}

	private Integer likeManagementRowCount(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		return jdbcTemplate.queryForObject(
				"""
						select count(*)
						from like_management
						where member_uuid = ? and like_type = ? and target_uuid = ?
						""",
				Integer.class,
				memberUuid.toString(),
				likeType.name(),
				targetUuid.toString()
		);
	}

	private Integer outboxEventCount(UUID targetUuid, LikeEventType eventType) {
		return jdbcTemplate.queryForObject(
				"""
						select count(*)
						from like_outbox
						where payload like ? and event_type = ?
						""",
				Integer.class,
				"%" + targetUuid + "%",
				eventType.name()
		);
	}

	private Long dbCounter(LikeType likeType, UUID targetUuid) {
		return jdbcTemplate.query(
				"""
						select like_count
						from like_target_counter
						where like_type = ? and target_uuid = ?
						""",
				rs -> rs.next() ? rs.getLong(1) : null,
				likeType.name(),
				targetUuid.toString()
		);
	}

	private Integer inboxCount() {
		return jdbcTemplate.queryForObject("select count(*) from like_count_inbox", Integer.class);
	}
}
