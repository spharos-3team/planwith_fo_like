package com.planwith.planwith_fo_like.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.command.RemoveLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.in.RemoveLikeUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeManagementPort;
import com.planwith.planwith_fo_like.application.query.LikeCommandResult;
import com.planwith.planwith_fo_like.domain.model.LikeManagement;
import com.planwith.planwith_fo_like.domain.model.LikeType;

@ActiveProfiles("test")
@SpringBootTest
class LikeAddRemoveIntegrationTest {

	@Autowired
	private AddLikeUseCase addLikeUseCase;

	@Autowired
	private RemoveLikeUseCase removeLikeUseCase;

	@Autowired
	private LikeManagementPort likeManagementPort;

	@Test
	void likeUnlikeAndRelikeKeepSingleRowAndSeparateCommands() {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();
		AddLikeCommand addCommand = new AddLikeCommand(memberUuid, LikeType.STORY, targetUuid, ownerUuid);
		RemoveLikeCommand removeCommand = new RemoveLikeCommand(memberUuid, LikeType.STORY, targetUuid, ownerUuid);

		LikeCommandResult firstLike = addLikeUseCase.add(addCommand);
		LikeManagement created = likeManagementPort.findByMemberAndTarget(memberUuid, LikeType.STORY, targetUuid)
				.orElseThrow();

		assertThat(firstLike.liked()).isTrue();
		assertThat(firstLike.alreadyApplied()).isFalse();
		assertThat(firstLike.likeCount()).isEqualTo(1L);
		assertThat(created.isActive()).isTrue();
		assertThat(created.deletedAt()).isNull();

		LikeCommandResult duplicateLike = addLikeUseCase.add(addCommand);
		assertThat(duplicateLike.liked()).isTrue();
		assertThat(duplicateLike.alreadyApplied()).isTrue();
		assertThat(duplicateLike.likeCount()).isEqualTo(1L);
		assertThat(likeManagementPort.findByMemberAndTarget(memberUuid, LikeType.STORY, targetUuid))
				.map(LikeManagement::likeUuid)
				.contains(created.likeUuid());

		LikeCommandResult unlike = removeLikeUseCase.remove(removeCommand);
		LikeManagement cancelled = likeManagementPort.findByMemberAndTarget(memberUuid, LikeType.STORY, targetUuid)
				.orElseThrow();

		assertThat(unlike.liked()).isFalse();
		assertThat(unlike.alreadyApplied()).isFalse();
		assertThat(unlike.likeCount()).isZero();
		assertThat(cancelled.likeUuid()).isEqualTo(created.likeUuid());
		assertThat(cancelled.isDeleted()).isTrue();
		assertThat(cancelled.deletedAt()).isNotNull();

		LikeCommandResult duplicateUnlike = removeLikeUseCase.remove(removeCommand);
		assertThat(duplicateUnlike.liked()).isFalse();
		assertThat(duplicateUnlike.alreadyApplied()).isTrue();
		assertThat(duplicateUnlike.likeCount()).isZero();

		LikeCommandResult relike = addLikeUseCase.add(addCommand);
		LikeManagement restored = likeManagementPort.findByMemberAndTarget(memberUuid, LikeType.STORY, targetUuid)
				.orElseThrow();

		assertThat(relike.liked()).isTrue();
		assertThat(relike.alreadyApplied()).isFalse();
		assertThat(relike.likeCount()).isEqualTo(1L);
		assertThat(restored.likeUuid()).isEqualTo(created.likeUuid());
		assertThat(restored.isActive()).isTrue();
		assertThat(restored.deletedAt()).isNull();
	}
}
