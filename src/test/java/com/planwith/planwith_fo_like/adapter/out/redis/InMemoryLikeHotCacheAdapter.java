package com.planwith.planwith_fo_like.adapter.out.redis;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.domain.model.TargetType;

@Profile("test")
@Component
public class InMemoryLikeHotCacheAdapter implements LikeHotCachePort {

	private final ConcurrentHashMap<String, String> values = new ConcurrentHashMap<>();

	@Override
	public boolean tryAcquireDuplicateGuard(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		return values.putIfAbsent(guardKey(memberUuid, targetType, targetUuid), "1") == null;
	}

	@Override
	public void releaseDuplicateGuard(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		values.remove(guardKey(memberUuid, targetType, targetUuid));
	}

	@Override
	public Optional<Boolean> findLiked(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		String value = values.get(stateKey(memberUuid, targetType, targetUuid));
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of("1".equals(value));
	}

	@Override
	public void markLiked(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		values.put(stateKey(memberUuid, targetType, targetUuid), "1");
	}

	@Override
	public void markUnliked(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		values.put(stateKey(memberUuid, targetType, targetUuid), "0");
	}

	@Override
	public Optional<Long> findCount(TargetType targetType, UUID targetUuid) {
		String value = values.get(countKey(targetType, targetUuid));
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(Long.parseLong(value));
	}

	@Override
	public void saveCount(TargetType targetType, UUID targetUuid, long likeCount) {
		values.put(countKey(targetType, targetUuid), Long.toString(likeCount));
	}

	public void clear() {
		values.clear();
	}

	private static String stateKey(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		return "state:" + targetType + ":" + targetUuid + ":" + memberUuid;
	}

	private static String countKey(TargetType targetType, UUID targetUuid) {
		return "count:" + targetType + ":" + targetUuid;
	}

	private static String guardKey(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		return "guard:" + targetType + ":" + targetUuid + ":" + memberUuid;
	}
}
