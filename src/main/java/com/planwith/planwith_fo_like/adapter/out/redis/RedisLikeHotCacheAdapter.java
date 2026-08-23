package com.planwith.planwith_fo_like.adapter.out.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_like.application.port.out.LikeHotCachePort;
import com.planwith.planwith_fo_like.config.LikeCacheProperties;
import com.planwith.planwith_fo_like.domain.model.TargetType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("!test")
@Component
public class RedisLikeHotCacheAdapter implements LikeHotCachePort {

	private static final String LIKED = "1";
	private static final String UNLIKED = "0";

	private final StringRedisTemplate redisTemplate;
	private final LikeCacheProperties properties;

	public RedisLikeHotCacheAdapter(StringRedisTemplate redisTemplate, LikeCacheProperties properties) {
		this.redisTemplate = redisTemplate;
		this.properties = properties;
	}

	@Override
	public boolean tryAcquireDuplicateGuard(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		try {
			Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
					properties.guardKey(memberUuid, targetType, targetUuid),
					LIKED,
					positive(properties.getGuardTtl(), Duration.ofSeconds(5))
			);
			if (Boolean.TRUE.equals(acquired)) {
				return true;
			}
			log.debug("RedisLikeHotCacheAdapter : tryAcquireDuplicateGuard : 중복 요청 사전 차단 - memberUuid={}, targetUuid={}",
					memberUuid, targetUuid);
			return false;
		} catch (RuntimeException exception) {
			log.warn("RedisLikeHotCacheAdapter : tryAcquireDuplicateGuard : Redis 가드 실패로 DB UNIQUE 경로 유지 - memberUuid={}",
					memberUuid);
			return true;
		}
	}

	@Override
	public void releaseDuplicateGuard(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		try {
			redisTemplate.delete(properties.guardKey(memberUuid, targetType, targetUuid));
		} catch (RuntimeException exception) {
			log.warn("RedisLikeHotCacheAdapter : releaseDuplicateGuard : Redis 가드 해제 실패 - memberUuid={}", memberUuid);
		}
	}

	@Override
	public Optional<Boolean> findLiked(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		try {
			String value = redisTemplate.opsForValue().get(properties.stateKey(memberUuid, targetType, targetUuid));
			if (value == null) {
				return Optional.empty();
			}
			return Optional.of(LIKED.equals(value));
		} catch (RuntimeException exception) {
			log.warn("RedisLikeHotCacheAdapter : findLiked : Redis 조회 실패로 MySQL 조회로 전환 - memberUuid={}", memberUuid);
			return Optional.empty();
		}
	}

	@Override
	public void markLiked(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		writeState(memberUuid, targetType, targetUuid, LIKED);
	}

	@Override
	public void markUnliked(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		writeState(memberUuid, targetType, targetUuid, UNLIKED);
	}

	@Override
	public Optional<Long> findCount(TargetType targetType, UUID targetUuid) {
		try {
			String value = redisTemplate.opsForValue().get(properties.countKey(targetType, targetUuid));
			if (value == null || value.isBlank()) {
				return Optional.empty();
			}
			return Optional.of(Long.parseLong(value));
		} catch (RuntimeException exception) {
			log.warn("RedisLikeHotCacheAdapter : findCount : Redis 카운터 조회 실패로 MySQL 조회로 전환 - targetUuid={}",
					targetUuid);
			return Optional.empty();
		}
	}

	@Override
	public void saveCount(TargetType targetType, UUID targetUuid, long likeCount) {
		try {
			redisTemplate.opsForValue().set(
					properties.countKey(targetType, targetUuid),
					Long.toString(likeCount),
					positive(properties.getTtl(), Duration.ofMinutes(10))
			);
		} catch (RuntimeException exception) {
			log.warn("RedisLikeHotCacheAdapter : saveCount : Redis 카운터 저장 실패 - targetUuid={}", targetUuid);
		}
	}

	private void writeState(UUID memberUuid, TargetType targetType, UUID targetUuid, String value) {
		try {
			redisTemplate.opsForValue().set(
					properties.stateKey(memberUuid, targetType, targetUuid),
					value,
					positive(properties.getTtl(), Duration.ofMinutes(10))
			);
		} catch (RuntimeException exception) {
			log.warn("RedisLikeHotCacheAdapter : writeState : Redis 상태 저장 실패 - memberUuid={}", memberUuid);
		}
	}

	private static Duration positive(Duration value, Duration fallback) {
		if (value == null || value.isZero() || value.isNegative()) {
			return fallback;
		}
		return value;
	}
}
