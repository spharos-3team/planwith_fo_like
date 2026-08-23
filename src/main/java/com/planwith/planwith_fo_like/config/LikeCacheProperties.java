package com.planwith.planwith_fo_like.config;

import java.time.Duration;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.planwith.planwith_fo_like.domain.model.TargetType;

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

	public String stateKey(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		return keyPrefix + ":state:" + targetType.name() + ":" + targetUuid + ":" + memberUuid;
	}

	public String countKey(TargetType targetType, UUID targetUuid) {
		return keyPrefix + ":count:" + targetType.name() + ":" + targetUuid;
	}

	public String guardKey(UUID memberUuid, TargetType targetType, UUID targetUuid) {
		return keyPrefix + ":guard:" + targetType.name() + ":" + targetUuid + ":" + memberUuid;
	}
}
