package com.planwith.planwith_fo_like.application.service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_like.domain.event.LikeCreatedEvent;
import com.planwith.planwith_fo_like.domain.event.LikeRemovedEvent;
import com.planwith.planwith_fo_like.domain.exception.LikeDomainException;

@Component
public class LikeEventPayloadWriter {

	private final ObjectMapper objectMapper;

	public LikeEventPayloadWriter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String writeCreated(LikeCreatedEvent event) {
		return write(toMap(
				event.eventUuid(),
				event.targetType().name(),
				event.targetUuid(),
				event.targetOwnerUuid(),
				event.likerUuid(),
				event.occurredAt(),
				event.sourceVersion()
		));
	}

	public String writeRemoved(LikeRemovedEvent event) {
		return write(toMap(
				event.eventUuid(),
				event.targetType().name(),
				event.targetUuid(),
				event.targetOwnerUuid(),
				event.likerUuid(),
				event.occurredAt(),
				event.sourceVersion()
		));
	}

	private static Map<String, Object> toMap(
			UUID eventUuid,
			String targetType,
			UUID targetUuid,
			UUID targetOwnerUuid,
			UUID likerUuid,
			Instant occurredAt,
			long sourceVersion
	) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("eventUuid", eventUuid.toString());
		payload.put("targetType", targetType);
		payload.put("targetUuid", targetUuid.toString());
		payload.put("targetOwnerUuid", targetOwnerUuid.toString());
		payload.put("likerUuid", likerUuid.toString());
		payload.put("occurredAt", DateTimeFormatter.ISO_INSTANT.format(occurredAt));
		payload.put("sourceVersion", sourceVersion);
		return payload;
	}

	private String write(Map<String, Object> payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new LikeDomainException("LIKE_EVENT_PAYLOAD_ERROR", "좋아요 이벤트 페이로드 직렬화에 실패했습니다.");
		}
	}
}
