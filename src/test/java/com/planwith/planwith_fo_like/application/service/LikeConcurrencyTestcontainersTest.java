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
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.port.in.AddLikeUseCase;
import com.planwith.planwith_fo_like.application.port.in.GetTargetLikeCountQueryUseCase;
import com.planwith.planwith_fo_like.application.query.GetTargetLikeCountQuery;
import com.planwith.planwith_fo_like.application.query.LikeCommandResult;
import com.planwith.planwith_fo_like.domain.model.TargetType;

/**
 * 동일 Like 동시 요청은 Redis가 아니라 MySQL UNIQUE + 멱등 처리가 최종 방어선이다.
 * Docker가 있을 때 LIKE_CONCURRENCY_TEST=true 로 실행한다.
 */
@EnabledIfEnvironmentVariable(named = "LIKE_CONCURRENCY_TEST", matches = "true")
@ActiveProfiles("test")
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class LikeConcurrencyTestcontainersTest {

	@Container
	@ServiceConnection
	static final MySQLContainer mysql = new MySQLContainer("mysql:8.4");

	@DynamicPropertySource
	static void overrideDatasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
	}

	@Autowired
	private AddLikeUseCase addLikeUseCase;

	@Autowired
	private GetTargetLikeCountQueryUseCase getTargetLikeCountQueryUseCase;

	@Test
	void concurrentSameLikeRequestsKeepSingleRelationAndCounter() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();
		AddLikeCommand command = new AddLikeCommand(memberUuid, TargetType.STORY, targetUuid, ownerUuid);

		int threadCount = 16;
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
			assertThat(results.stream().filter(result -> !result.alreadyApplied()).count()).isEqualTo(1);
			assertThat(results).allMatch(LikeCommandResult::liked);
			assertThat(getTargetLikeCountQueryUseCase.get(new GetTargetLikeCountQuery(TargetType.STORY, targetUuid))
					.likeCount()).isEqualTo(1);
		} finally {
			executor.shutdownNow();
		}
	}
}
