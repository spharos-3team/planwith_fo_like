package com.planwith.planwith_fo_like.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_like.application.command.LikeCountApplyCommand;
import com.planwith.planwith_fo_like.domain.event.LikeEvent;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeType;

class LikeEventPayloadReaderTest {

	@Test
	void readsLikeAndUnlikePayloads() {
		Instant now = Instant.parse("2026-08-23T00:00:00Z");
		LikeManagement like = LikeManagement.create(UUID.randomUUID(), UUID.randomUUID(), LikeType.COMMENT, now);
		LikeEvent event = LikeEvent.unlike(like, now);
		ObjectMapper objectMapper = new ObjectMapper();
		String payload = new LikeEventPayloadWriter(objectMapper).write(event, UUID.randomUUID(), 0L);

		LikeCountApplyCommand command = new LikeEventPayloadReader(objectMapper).read(payload);

		assertThat(command.eventId()).isEqualTo(event.eventId());
		assertThat(command.eventType()).isEqualTo(LikeEventType.UNLIKE);
		assertThat(command.likeType()).isEqualTo(LikeType.COMMENT);
		assertThat(command.targetUuid()).isEqualTo(like.targetUuid());
	}
}
