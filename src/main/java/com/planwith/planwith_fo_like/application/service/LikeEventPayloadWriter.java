package com.planwith.planwith_fo_like.application.service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_like.domain.event.LikeEvent;
import com.planwith.planwith_fo_like.domain.exception.LikeDomainException;

@Component
public class LikeEventPayloadWriter {

	private final ObjectMapper objectMapper;

	public LikeEventPayloadWriter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String write(LikeEvent event, UUID targetOwnerUuid, long sourceVersion) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("eventId", event.eventId().toString());
		payload.put("eventUuid", event.eventId().toString());
		payload.put("eventType", event.eventType().name());
		payload.put("memberUuid", event.memberUuid().toString());
		payload.put("likerUuid", event.memberUuid().toString());
		payload.put("likeUuid", event.likeUuid().toString());
		payload.put("likeType", event.likeType().name());
		payload.put("targetType", event.likeType().name());
		payload.put("targetUuid", event.targetUuid().toString());
		payload.put("targetOwnerUuid", targetOwnerUuid == null ? null : targetOwnerUuid.toString());
		payload.put("occurredAt", DateTimeFormatter.ISO_INSTANT.format(event.occurredAt()));
		payload.put("sourceVersion", sourceVersion);
		return write(payload);
	}

	private String write(Map<String, Object> payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new LikeDomainException("LIKE_EVENT_PAYLOAD_ERROR", "좋아요 이벤트 페이로드 직렬화에 실패했습니다.");
		}
	}
}
