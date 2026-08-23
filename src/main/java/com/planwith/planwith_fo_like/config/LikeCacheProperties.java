package com.planwith.planwith_fo_like.config;

import java.time.Duration;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.planwith.planwith_fo_like.domain.model.LikeType;

@ConfigurationProperties(prefix = "like.cache")
public class LikeCacheProperties {

	private String keyPrefix = "like";
	private Duration ttl = Duration.ofMinutes(10);
	private Duration guardTtl = Duration.ofSeconds(5);

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public Duration getTtl() {
		return ttl;
	}

	public void setTtl(Duration ttl) {
		this.ttl = ttl;
	}

	public Duration getGuardTtl() {
		return guardTtl;
	}

	public void setGuardTtl(Duration guardTtl) {
		this.guardTtl = guardTtl;
	}

	public String stateKey(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		return keyPrefix + ":state:" + likeType.name() + ":" + targetUuid + ":" + memberUuid;
	}

	public String countKey(LikeType likeType, UUID targetUuid) {
		return keyPrefix + ":count:" + likeType.name() + ":" + targetUuid;
	}

	public String guardKey(UUID memberUuid, LikeType likeType, UUID targetUuid) {
		return keyPrefix + ":guard:" + likeType.name() + ":" + targetUuid + ":" + memberUuid;
	}
}
