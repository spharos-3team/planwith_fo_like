package com.planwith.planwith_fo_like.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.command.LikeCountApplyCommand;
import com.planwith.planwith_fo_like.application.command.RemoveLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.in.ApplyLikeCountUseCase;
import com.planwith.planwith_fo_like.application.port.in.RemoveLikeUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.domain.event.LikeEventType;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.model.LikeType;

@ActiveProfiles("test")
@SpringBootTest
class ApplyLikeCountIntegrationTest {

	@Autowired
	private AddLikeUseCase addLikeUseCase;

	@Autowired
	private RemoveLikeUseCase removeLikeUseCase;

	@Autowired
	private ApplyLikeCountUseCase applyLikeCountUseCase;

	@Autowired
	private LikeEventPayloadReader payloadReader;

	@Autowired
	private LikeTargetCounterPort likeTargetCounterPort;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void commandDoesNotWriteCounterAndConsumerAppliesPlusMinusWithFloor() {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();

		addLikeUseCase.add(new AddLikeCommand(memberUuid, LikeType.STORY, targetUuid, ownerUuid));

		assertThat(likeTargetCounterPort.findByTarget(LikeType.STORY, targetUuid)).isEmpty();

		LikeCountTestSupport.applyOutboxEvents(jdbcTemplate, payloadReader, applyLikeCountUseCase);
		assertThat(likeTargetCounterPort.findByTarget(LikeType.STORY, targetUuid)
				.map(LikeTargetCounter::likeCount))
				.contains(1L);

		LikeCountTestSupport.applyOutboxEvents(jdbcTemplate, payloadReader, applyLikeCountUseCase);
		assertThat(likeTargetCounterPort.findByTarget(LikeType.STORY, targetUuid)
				.map(LikeTargetCounter::likeCount))
				.contains(1L);

		removeLikeUseCase.remove(new RemoveLikeCommand(memberUuid, LikeType.STORY, targetUuid, ownerUuid));
		LikeCountTestSupport.applyOutboxEvents(jdbcTemplate, payloadReader, applyLikeCountUseCase);
		assertThat(likeTargetCounterPort.findByTarget(LikeType.STORY, targetUuid)
				.map(LikeTargetCounter::likeCount))
				.contains(0L);

		applyLikeCountUseCase.apply(new LikeCountApplyCommand(
				UUID.randomUUID(),
				LikeEventType.UNLIKE,
				LikeType.STORY,
				targetUuid
		));
		assertThat(likeTargetCounterPort.findByTarget(LikeType.STORY, targetUuid)
				.map(LikeTargetCounter::likeCount))
				.contains(0L);
	}
}
