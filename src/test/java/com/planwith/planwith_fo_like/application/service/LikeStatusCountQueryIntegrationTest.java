package com.planwith.planwith_fo_like.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_like.adapter.out.redis.InMemoryLikeHotCacheAdapter;
import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.command.RemoveLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.in.ApplyLikeCountUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetMyLikeStatusQueryUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetTargetLikeCountQueryUseCase;
import com.planwith.planwith_fo_like.application.port.in.RemoveLikeUseCase;
import com.planwith.planwith_fo_like.application.port.out.LikeTargetCounterPort;
import com.planwith.planwith_fo_like.application.query.GetMyLikeStatusQuery;
import com.planwith.planwith_fo_like.application.query.GetTargetLikeCountQuery;
import com.planwith.planwith_fo_like.domain.model.LikeTargetCounter;
import com.planwith.planwith_fo_like.domain.model.LikeType;

@ActiveProfiles("test")
@SpringBootTest
class LikeStatusCountQueryIntegrationTest {

	@Autowired
	private AddLikeUseCase addLikeUseCase;

	@Autowired
	private RemoveLikeUseCase removeLikeUseCase;

	@Autowired
	private GetMyLikeStatusQueryUseCase getMyLikeStatusQueryUseCase;

	@Autowired
	private GetTargetLikeCountQueryUseCase getTargetLikeCountQueryUseCase;

	@Autowired
	private LikeTargetCounterPort likeTargetCounterPort;

	@Autowired
	private ApplyLikeCountUseCase applyLikeCountUseCase;

	@Autowired
	private LikeEventPayloadReader payloadReader;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private InMemoryLikeHotCacheAdapter likeHotCacheAdapter;

	@Test
	void statusAndCountReadCounterNotLedgerAggregate() {
		UUID memberUuid = UUID.randomUUID();
		UUID otherMemberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();

		assertThat(getMyLikeStatusQueryUseCase.get(new GetMyLikeStatusQuery(memberUuid, LikeType.STORY, targetUuid))
				.liked()).isFalse();
		assertThat(getTargetLikeCountQueryUseCase.get(new GetTargetLikeCountQuery(LikeType.STORY, targetUuid))
				.likeCount()).isZero();

		addLikeUseCase.add(new AddLikeCommand(memberUuid, LikeType.STORY, targetUuid, ownerUuid));
		addLikeUseCase.add(new AddLikeCommand(otherMemberUuid, LikeType.STORY, targetUuid, ownerUuid));
		LikeCountTestSupport.applyOutboxEvents(jdbcTemplate, payloadReader, applyLikeCountUseCase);

		assertThat(getMyLikeStatusQueryUseCase.get(new GetMyLikeStatusQuery(memberUuid, LikeType.STORY, targetUuid))
				.liked()).isTrue();
		assertThat(getMyLikeStatusQueryUseCase.get(new GetMyLikeStatusQuery(UUID.randomUUID(), LikeType.STORY, targetUuid))
				.liked()).isFalse();
		assertThat(getTargetLikeCountQueryUseCase.get(new GetTargetLikeCountQuery(LikeType.STORY, targetUuid))
				.likeCount()).isEqualTo(2L);
		assertThat(likeTargetCounterPort.findByTarget(LikeType.STORY, targetUuid)
				.map(LikeTargetCounter::likeCount))
				.contains(2L);

		removeLikeUseCase.remove(new RemoveLikeCommand(memberUuid, LikeType.STORY, targetUuid, ownerUuid));
		LikeCountTestSupport.applyOutboxEvents(jdbcTemplate, payloadReader, applyLikeCountUseCase);

		assertThat(getMyLikeStatusQueryUseCase.get(new GetMyLikeStatusQuery(memberUuid, LikeType.STORY, targetUuid))
				.liked()).isFalse();
		assertThat(getTargetLikeCountQueryUseCase.get(new GetTargetLikeCountQuery(LikeType.STORY, targetUuid))
				.likeCount()).isEqualTo(1L);
		assertThat(likeTargetCounterPort.findByTarget(LikeType.STORY, targetUuid)
				.map(LikeTargetCounter::likeCount))
				.contains(1L);
	}

	@Test
	void redisMissFallsBackToMysqlCounterAndActiveLike() {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		addLikeUseCase.add(new AddLikeCommand(memberUuid, LikeType.COMMENT, targetUuid, UUID.randomUUID()));
		LikeCountTestSupport.applyOutboxEvents(jdbcTemplate, payloadReader, applyLikeCountUseCase);

		likeHotCacheAdapter.clear();

		assertThat(getMyLikeStatusQueryUseCase.get(new GetMyLikeStatusQuery(memberUuid, LikeType.COMMENT, targetUuid))
				.liked()).isTrue();
		assertThat(getTargetLikeCountQueryUseCase.get(new GetTargetLikeCountQuery(LikeType.COMMENT, targetUuid))
				.likeCount()).isEqualTo(1L);
		assertThat(likeTargetCounterPort.findByTarget(LikeType.COMMENT, targetUuid)
				.map(LikeTargetCounter::likeCount))
				.contains(1L);
	}
}
