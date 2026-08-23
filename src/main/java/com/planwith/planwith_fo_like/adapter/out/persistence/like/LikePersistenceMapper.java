package com.planwith.planwith_fo_like.adapter.out.persistence.like;

import com.planwith.planwith_fo_like.domain.model.LikeRelation;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;

final class LikePersistenceMapper {

	private LikePersistenceMapper() {
	}

	static LikeManagementJpaEntity toEntity(LikeRelation relation) {
		return new LikeManagementJpaEntity(
				relation.likeUuid(),
				relation.memberUuid(),
				relation.targetType(),
				relation.targetUuid(),
				relation.targetOwnerUuid(),
				relation.createdAt()
		);
	}

	static LikeRelation toDomain(LikeManagementJpaEntity entity) {
		return LikeRelation.restore(
				entity.likeId(),
				entity.likeUuid(),
				entity.memberUuid(),
				entity.targetType(),
				entity.targetUuid(),
				entity.targetOwnerUuid(),
				entity.createdAt()
		);
	}

	static LikeTargetCounter toDomain(LikeTargetCounterJpaEntity entity) {
		return LikeTargetCounter.restore(
				entity.counterId(),
				entity.targetType(),
				entity.targetUuid(),
				entity.likeCount(),
				entity.version()
		);
	}
}
