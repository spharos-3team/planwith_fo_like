package com.planwith.planwith_fo_like.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_like.domain.exception.LikeDomainException;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeType;

class LikeEventTest {

	private static final Instant NOW = Instant.parse("2026-08-23T00:00:00Z");

	@Test
	void likeAndUnlikeCarryLikeTypeAndTargetUuid() {
		LikeManagement storyLike = LikeManagement.create(UUID.randomUUID(), UUID.randomUUID(), LikeType.STORY, NOW);
		LikeManagement commentLike = LikeManagement.create(UUID.randomUUID(), UUID.randomUUID(), LikeType.COMMENT, NOW);

		LikeEvent likeEvent = LikeEvent.like(storyLike, NOW);
		LikeEvent unlikeEvent = LikeEvent.unlike(commentLike, NOW.plusSeconds(1));

		assertThat(likeEvent.eventType()).isEqualTo(LikeEventType.LIKE);
		assertThat(likeEvent.eventType().isLike()).isTrue();
		assertThat(likeEvent.likeType()).isEqualTo(LikeType.STORY);
		assertThat(likeEvent.targetUuid()).isEqualTo(storyLike.targetUuid());
		assertThat(likeEvent.likeUuid()).isEqualTo(storyLike.likeUuid());
		assertThat(likeEvent.memberUuid()).isEqualTo(storyLike.memberUuid());
		assertThat(likeEvent.isStoryLike()).isTrue();

		assertThat(unlikeEvent.eventType()).isEqualTo(LikeEventType.UNLIKE);
		assertThat(unlikeEvent.eventType().isUnlike()).isTrue();
		assertThat(unlikeEvent.likeType()).isEqualTo(LikeType.COMMENT);
		assertThat(unlikeEvent.targetUuid()).isEqualTo(commentLike.targetUuid());
		assertThat(unlikeEvent.isCommentLike()).isTrue();
	}

	@Test
	void rejectsMissingRequiredFields() {
		assertThatThrownBy(() -> new LikeEvent(
				UUID.randomUUID(),
				LikeEventType.LIKE,
				UUID.randomUUID(),
				UUID.randomUUID(),
				null,
				UUID.randomUUID(),
				NOW
		)).isInstanceOf(LikeDomainException.class);
		assertThatThrownBy(() -> LikeEvent.like(null, NOW)).isInstanceOf(LikeDomainException.class);
	}
}
