package com.planwith.planwith_fo_like.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetLikeSnapshotQueryUseCase;
import com.planwith.planwith_fo_like.application.query.GetLikeSnapshotQuery;
import com.planwith.planwith_fo_like.application.query.GetLikeSnapshotsQuery;
import com.planwith.planwith_fo_like.application.query.LikeSnapshotView;
import com.planwith.planwith_fo_like.domain.exception.InvalidLikeTargetException;
import com.planwith.planwith_fo_like.domain.model.LikeType;

@ActiveProfiles("test")
@SpringBootTest
class LikeSnapshotQueryIntegrationTest {

	@Autowired
	private AddLikeUseCase addLikeUseCase;

	@Autowired
	private GetLikeSnapshotQueryUseCase getLikeSnapshotQueryUseCase;

	@Test
	void emptySnapshotProvidesOptimisticCountsForScreen() {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();

		LikeSnapshotView snapshot = getLikeSnapshotQueryUseCase.get(
				new GetLikeSnapshotQuery(memberUuid, LikeType.STORY, targetUuid)
		);

		assertThat(snapshot.liked()).isFalse();
		assertThat(snapshot.likeCount()).isZero();
		assertThat(snapshot.optimisticLikeCount()).isEqualTo(1L);
		assertThat(snapshot.optimisticUnlikeCount()).isZero();
	}

	@Test
	void snapshotAfterLikeAndGuestViewAreIndependentByType() {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();

		addLikeUseCase.add(new AddLikeCommand(memberUuid, LikeType.STORY, targetUuid, ownerUuid));
		addLikeUseCase.add(new AddLikeCommand(memberUuid, LikeType.COMMENT, targetUuid, ownerUuid));

		LikeSnapshotView storySnapshot = getLikeSnapshotQueryUseCase.get(
				new GetLikeSnapshotQuery(memberUuid, LikeType.STORY, targetUuid)
		);
		LikeSnapshotView commentSnapshot = getLikeSnapshotQueryUseCase.get(
				new GetLikeSnapshotQuery(memberUuid, LikeType.COMMENT, targetUuid)
		);
		LikeSnapshotView guestSnapshot = getLikeSnapshotQueryUseCase.get(
				new GetLikeSnapshotQuery(null, LikeType.STORY, targetUuid)
		);

		assertThat(storySnapshot.liked()).isTrue();
		assertThat(storySnapshot.likeCount()).isEqualTo(1L);
		assertThat(storySnapshot.optimisticLikeCount()).isEqualTo(2L);
		assertThat(storySnapshot.optimisticUnlikeCount()).isZero();
		assertThat(commentSnapshot.liked()).isTrue();
		assertThat(commentSnapshot.likeCount()).isEqualTo(1L);
		assertThat(guestSnapshot.liked()).isFalse();
		assertThat(guestSnapshot.likeCount()).isEqualTo(1L);
	}

	@Test
	void batchSnapshotDeduplicatesAndRejectsOverflow() {
		UUID memberUuid = UUID.randomUUID();
		UUID firstComment = UUID.randomUUID();
		UUID secondComment = UUID.randomUUID();
		addLikeUseCase.add(new AddLikeCommand(memberUuid, LikeType.COMMENT, firstComment, UUID.randomUUID()));

		List<LikeSnapshotView> snapshots = getLikeSnapshotQueryUseCase.getAll(
				new GetLikeSnapshotsQuery(
						memberUuid,
						LikeType.COMMENT,
						List.of(firstComment, firstComment, secondComment)
				)
		);

		assertThat(snapshots).hasSize(2);
		assertThat(snapshots.get(0).targetUuid()).isEqualTo(firstComment);
		assertThat(snapshots.get(0).liked()).isTrue();
		assertThat(snapshots.get(0).likeCount()).isEqualTo(1L);
		assertThat(snapshots.get(1).targetUuid()).isEqualTo(secondComment);
		assertThat(snapshots.get(1).liked()).isFalse();
		assertThat(snapshots.get(1).likeCount()).isZero();

		List<UUID> overflow = new ArrayList<>();
		for (int index = 0; index < GetLikeSnapshotService.MAX_SNAPSHOT_TARGETS + 1; index++) {
			overflow.add(UUID.randomUUID());
		}
		assertThatThrownBy(() -> getLikeSnapshotQueryUseCase.getAll(
				new GetLikeSnapshotsQuery(memberUuid, LikeType.COMMENT, overflow)
		)).isInstanceOf(InvalidLikeTargetException.class);
	}
}
