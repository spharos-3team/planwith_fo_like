package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;

final class LikePersistenceMapper {

	private LikePersistenceMapper() {
	}

	static LikeManagementJpaEntity toEntity(LikeManagement like) {
		return new LikeManagementJpaEntity(
				like.likeUuid(),
				like.memberUuid(),
				like.targetUuid(),
				like.likeType(),
				like.createdAt(),
				like.updatedAt(),
				like.deletedAt()
		);
	}

	static LikeManagement toDomain(LikeManagementJpaEntity entity) {
		return LikeManagement.restore(
				entity.likeId(),
				entity.likeUuid(),
				entity.memberUuid(),
				entity.targetUuid(),
				entity.likeType(),
				entity.createdAt(),
				entity.updatedAt(),
				entity.deletedAt()
		);
	}

	static LikeTargetCounter toDomain(LikeTargetCounterJpaEntity entity) {
		return LikeTargetCounter.restore(
				entity.likeType(),
				entity.targetUuid(),
				entity.likeCount(),
				entity.updatedAt()
		);
	}
}
