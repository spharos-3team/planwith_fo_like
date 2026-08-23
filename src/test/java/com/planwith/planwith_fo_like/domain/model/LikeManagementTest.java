package com.planwith.planwith_fo_like.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class LikeManagementTest {

	@Test
	void storyAndCommentLikesAreDistinguishedInSameDomain() {
		Instant now = Instant.parse("2026-08-23T00:00:00Z");
		UUID memberUuid = UUID.randomUUID();
		UUID storyUuid = UUID.randomUUID();
		UUID commentUuid = UUID.randomUUID();

		LikeManagement storyLike = LikeManagement.create(memberUuid, storyUuid, LikeType.STORY, now);
		LikeManagement commentLike = LikeManagement.create(memberUuid, commentUuid, LikeType.COMMENT, now);

		assertThat(storyLike.isStoryLike()).isTrue();
		assertThat(storyLike.isCommentLike()).isFalse();
		assertThat(storyLike.targetUuid()).isEqualTo(storyUuid);
		assertThat(commentLike.isCommentLike()).isTrue();
		assertThat(commentLike.isStoryLike()).isFalse();
		assertThat(commentLike.targetUuid()).isEqualTo(commentUuid);
		assertThat(storyLike.isActive()).isTrue();
	}

	@Test
	void softDeleteAndRestoreKeepSameLikeIdentity() {
		Instant createdAt = Instant.parse("2026-08-23T00:00:00Z");
		Instant deletedAt = Instant.parse("2026-08-23T01:00:00Z");
		Instant restoredAt = Instant.parse("2026-08-23T02:00:00Z");
		LikeManagement like = LikeManagement.create(UUID.randomUUID(), UUID.randomUUID(), LikeType.STORY, createdAt);

		like.markDeleted(deletedAt);

		assertThat(like.isDeleted()).isTrue();
		assertThat(like.isActive()).isFalse();
		assertThat(like.deletedAt()).isEqualTo(deletedAt);
		assertThat(like.updatedAt()).isEqualTo(deletedAt);

		like.restoreDeleted(restoredAt);

		assertThat(like.isActive()).isTrue();
		assertThat(like.deletedAt()).isNull();
		assertThat(like.updatedAt()).isEqualTo(restoredAt);
	}
}
