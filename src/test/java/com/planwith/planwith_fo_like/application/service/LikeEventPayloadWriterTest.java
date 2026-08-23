package com.planwith.planwith_fo_like.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_like.domain.event.LikeEvent;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeType;

class LikeEventPayloadWriterTest {

	@Test
	void payloadIncludesOfficialFieldsAndGradeAliases() throws Exception {
		Instant now = Instant.parse("2026-08-23T00:00:00Z");
		LikeManagement like = LikeManagement.create(UUID.randomUUID(), UUID.randomUUID(), LikeType.STORY, now);
		LikeEvent event = LikeEvent.like(like, now);
		UUID ownerUuid = UUID.randomUUID();

		String payload = new LikeEventPayloadWriter(new ObjectMapper()).write(event, ownerUuid, 3L);
		JsonNode json = new ObjectMapper().readTree(payload);

		assertThat(json.get("eventId").asText()).isEqualTo(event.eventId().toString());
		assertThat(json.get("eventUuid").asText()).isEqualTo(event.eventId().toString());
		assertThat(json.get("eventType").asText()).isEqualTo(LikeEventType.LIKE.name());
		assertThat(json.get("memberUuid").asText()).isEqualTo(like.memberUuid().toString());
		assertThat(json.get("likerUuid").asText()).isEqualTo(like.memberUuid().toString());
		assertThat(json.get("likeUuid").asText()).isEqualTo(like.likeUuid().toString());
		assertThat(json.get("likeType").asText()).isEqualTo("STORY");
		assertThat(json.get("targetType").asText()).isEqualTo("STORY");
		assertThat(json.get("targetUuid").asText()).isEqualTo(like.targetUuid().toString());
		assertThat(json.get("targetOwnerUuid").asText()).isEqualTo(ownerUuid.toString());
		assertThat(json.get("occurredAt").asText()).isEqualTo("2026-08-23T00:00:00Z");
		assertThat(json.get("sourceVersion").asLong()).isEqualTo(3L);
	}
}
