package com.planwith.planwith_fo_like.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetTargetLikeCountQueryUseCase;
import com.planwith.planwith_fo_like.application.query.GetTargetLikeCountQuery;
import com.planwith.planwith_fo_like.application.query.LikeCommandResult;
import com.planwith.planwith_fo_like.domain.model.LikeType;

@ActiveProfiles("test")
@SpringBootTest
class LikeConcurrencyIntegrationTest {

	@Autowired
	private AddLikeUseCase addLikeUseCase;

	@Autowired
	private GetTargetLikeCountQueryUseCase getTargetLikeCountQueryUseCase;

	@Test
	void concurrentSameLikeRequestsAreIdempotentAndKeepSingleCount() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();
		AddLikeCommand command = new AddLikeCommand(memberUuid, LikeType.COMMENT, targetUuid, ownerUuid);

		int threadCount = 12;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch ready = new CountDownLatch(threadCount);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<LikeCommandResult>> futures = new ArrayList<>();
		try {
			for (int i = 0; i < threadCount; i++) {
				futures.add(executor.submit(() -> {
					ready.countDown();
					start.await();
					return addLikeUseCase.add(command);
				}));
			}
			assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
			start.countDown();

			List<LikeCommandResult> results = new ArrayList<>();
			for (Future<LikeCommandResult> future : futures) {
				results.add(future.get(20, TimeUnit.SECONDS));
			}

			assertThat(results).hasSize(threadCount);
			assertThat(results).allMatch(LikeCommandResult::liked);
			assertThat(results.stream().filter(result -> !result.alreadyApplied()).count()).isEqualTo(1);
			assertThat(getTargetLikeCountQueryUseCase.get(new GetTargetLikeCountQuery(LikeType.COMMENT, targetUuid))
					.likeCount()).isEqualTo(1);
		} finally {
			executor.shutdownNow();
		}
	}
}
