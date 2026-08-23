package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_like.domain.model.LikeType;

public class LikeTargetCounterId implements Serializable {

	private LikeType likeType;
	private UUID targetUuid;

	public LikeTargetCounterId() {
	}

	public LikeTargetCounterId(LikeType likeType, UUID targetUuid) {
		this.likeType = likeType;
		this.targetUuid = targetUuid;
	}

	public LikeType getLikeType() {
		return likeType;
	}

	public void setLikeType(LikeType likeType) {
		this.likeType = likeType;
	}

	public UUID getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(UUID targetUuid) {
		this.targetUuid = targetUuid;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof LikeTargetCounterId that)) {
			return false;
		}
		return likeType == that.likeType && Objects.equals(targetUuid, that.targetUuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(likeType, targetUuid);
	}
}
