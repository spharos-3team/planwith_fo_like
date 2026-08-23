package com.planwith.planwith_fo_like.application.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_like.application.command.LikeCountApplyCommand;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.exception.LikeDomainException;
import com.planwith.planwith_fo_like.domain.model.LikeType;

@Component
public class LikeEventPayloadReader {

	private final ObjectMapper objectMapper;

	public LikeEventPayloadReader(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public LikeCountApplyCommand read(String payload) {
		try {
			JsonNode node = objectMapper.readTree(payload);
			return new LikeCountApplyCommand(
					parseUuid(text(node, "eventId", "eventUuid"), "이벤트 식별자가 올바르지 않습니다."),
					parseEventType(text(node, "eventType")),
					LikeType.from(text(node, "likeType", "targetType")),
					parseUuid(text(node, "targetUuid"), "대상 식별자가 올바르지 않습니다.")
			);
		} catch (JsonProcessingException exception) {
			throw new LikeDomainException("INVALID_LIKE_EVENT", "좋아요 이벤트 페이로드를 읽을 수 없습니다.");
		}
	}

	private static LikeEventType parseEventType(String rawType) {
		if (rawType == null || rawType.isBlank()) {
			throw new LikeDomainException("INVALID_LIKE_EVENT", "좋아요 이벤트 타입이 없습니다.");
		}
		String normalized = rawType.trim().toUpperCase();
		if ("LIKE_CREATED".equals(normalized) || LikeEventType.LIKE.name().equals(normalized)) {
			return LikeEventType.LIKE;
		}
		if ("LIKE_REMOVED".equals(normalized) || LikeEventType.UNLIKE.name().equals(normalized)) {
			return LikeEventType.UNLIKE;
		}
		throw new LikeDomainException("INVALID_LIKE_EVENT", "지원하지 않는 좋아요 이벤트 타입입니다.");
	}

	private static UUID parseUuid(String rawUuid, String message) {
		try {
			return UUID.fromString(rawUuid);
		} catch (RuntimeException exception) {
			throw new LikeDomainException("INVALID_LIKE_EVENT", message);
		}
	}

	private static String text(JsonNode node, String... fieldNames) {
		for (String fieldName : fieldNames) {
			JsonNode value = node.get(fieldName);
			if (value != null && !value.isNull() && !value.asText().isBlank()) {
				return value.asText();
			}
		}
		return "";
	}
}
