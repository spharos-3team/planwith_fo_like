package com.planwith.planwith_fo_like.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.command.RemoveLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.in.RemoveLikeUseCase;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.model.LikeType;

@ActiveProfiles("test")
@SpringBootTest
class LikeEventOutboxIntegrationTest {

	@Autowired
	private AddLikeUseCase addLikeUseCase;

	@Autowired
	private RemoveLikeUseCase removeLikeUseCase;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void likeAndUnlikeWriteOutboxEventsWithLikeType() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID storyUuid = UUID.randomUUID();
		UUID commentUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();

		addLikeUseCase.add(new AddLikeCommand(memberUuid, LikeType.STORY, storyUuid, ownerUuid));
		addLikeUseCase.add(new AddLikeCommand(memberUuid, LikeType.STORY, storyUuid, ownerUuid));
		addLikeUseCase.add(new AddLikeCommand(memberUuid, LikeType.COMMENT, commentUuid, ownerUuid));
		removeLikeUseCase.remove(new RemoveLikeCommand(memberUuid, LikeType.STORY, storyUuid, ownerUuid));

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"""
						select event_type, payload
						from like_outbox
						where payload like ?
						order by occurred_at asc, outbox_id asc
						""",
				"%" + memberUuid + "%"
		);

		assertThat(rows).hasSize(3);
		assertThat(rows.get(0).get("event_type")).isEqualTo(LikeEventType.LIKE.name());
		assertThat(rows.get(1).get("event_type")).isEqualTo(LikeEventType.LIKE.name());
		assertThat(rows.get(2).get("event_type")).isEqualTo(LikeEventType.UNLIKE.name());

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode storyLike = objectMapper.readTree((String) rows.get(0).get("payload"));
		JsonNode commentLike = objectMapper.readTree((String) rows.get(1).get("payload"));
		JsonNode storyUnlike = objectMapper.readTree((String) rows.get(2).get("payload"));

		assertThat(storyLike.get("likeType").asText()).isEqualTo("STORY");
		assertThat(storyLike.get("targetType").asText()).isEqualTo("STORY");
		assertThat(storyLike.get("targetUuid").asText()).isEqualTo(storyUuid.toString());
		assertThat(storyLike.get("memberUuid").asText()).isEqualTo(memberUuid.toString());
		assertThat(storyLike.get("likeUuid").asText()).isNotBlank();

		assertThat(commentLike.get("likeType").asText()).isEqualTo("COMMENT");
		assertThat(commentLike.get("targetUuid").asText()).isEqualTo(commentUuid.toString());

		assertThat(storyUnlike.get("eventType").asText()).isEqualTo("UNLIKE");
		assertThat(storyUnlike.get("likeType").asText()).isEqualTo("STORY");
		assertThat(storyUnlike.get("targetUuid").asText()).isEqualTo(storyUuid.toString());
		assertThat(storyUnlike.get("likeUuid").asText()).isEqualTo(storyLike.get("likeUuid").asText());
	}
}
