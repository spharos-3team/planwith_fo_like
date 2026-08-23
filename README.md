# Release Note / RM — planwith-fo-like

## 1. 서비스 개요

`planwith-fo-like`는 Story/Comment 좋아요의 **상태, 카운트, 이벤트**를 담당하는 독립 MSA 서비스입니다.

- 애플리케이션 이름: `planwith-fo-like`
- 포트: `8091` (서버는 `127.0.0.1:8091`, 다른 PC는 Gateway `:8000`만 사용)
- DB: `like_db` / `like_user` (공유 MySQL, 스키마 분리)
- 스택: Spring Boot, Java 17, Hexagonal, JPA, Redis, Kafka, Transactional Outbox
- 제3자 외부 API는 없습니다.

Story 상세 화면의 책임 분리는 다음과 같습니다.

```
Story Service  → 스토리 정보
Like Service   → 좋아요 수, 내 좋아요 여부
Comment Service → 댓글
```

Like 서비스는 Story/Comment 테이블을 조인하거나 FK를 두지 않습니다.

---

## 2. 도메인 범위

### 포함

| 대상 | 설명 |
|---|---|
| `LikeType` | `STORY`, `COMMENT`만 허용. 동일 `targetUuid`라도 타입별로 분리 |
| `LikeManagement` | 회원별 좋아요 원장. Soft Delete (`deleted_at`). UNIQUE(`member_uuid`, `like_type`, `target_uuid`) |
| `LikeTargetCounter` | 대상별 좋아요 수. Command가 직접 쓰지 않고 Kafka Consumer가 ±1 |
| `LikeEvent` | `LIKE` / `UNLIKE`. Outbox(`like_outbox`)에 기록 후 Kafka 발행 |
| `like_count_inbox` | 동일 `eventId` 재처리 시 Counter 중복 증가 방지 |
| Optimistic UI 계약 | 클릭 직후 화면 ±1, Command 실패 시 이전 값 롤백. UI 자체는 이 서비스에 없음 |

좋아요 생명주기:

1. 최초 좋아요: INSERT, `deleted_at = NULL`
2. 취소: 같은 행 `deleted_at = NOW()`
3. 재좋아요: 같은 행 `deleted_at = NULL` (행을 새로 만들지 않음)

Command HTTP `likeCount`는 Redis 기준 낙관 값(`current ± 1`)입니다. DB Counter 확정은 Consumer 이후입니다. Counter는 0 미만으로 내려가지 않습니다.

### 제외

- Story/Comment 존재 여부·권한 재검증 (MSA 내부 호출 없음)
- Member 권한/역할 Projection
- 화면 UI (React/HTML)
- PLAN 등 `STORY`/`COMMENT` 이외 타입

---

## 3. API 그룹

공통 prefix: `/api/v1/likes`  
회원 식별: `X-Member-UUID`  
선택 쿼리: `targetOwnerUuid` (이벤트 payload용)

### Command

| Method | URL | 설명 | 헤더 |
|---|---|---|---|
| PUT | `/api/v1/likes/{likeType}/{targetUuid}` | 좋아요. 중복이면 `alreadyApplied=true` | `X-Member-UUID` 필수 |
| DELETE | `/api/v1/likes/{likeType}/{targetUuid}` | 좋아요 취소. Soft Delete | `X-Member-UUID` 필수 |

응답: `memberUuid`, `likeType`, `targetUuid`, `liked`, `likeCount`, `alreadyApplied`

### Query

| Method | URL | 설명 | 헤더 |
|---|---|---|---|
| GET | `/api/v1/likes/{likeType}/{targetUuid}/me` | 내 좋아요 여부 `{ liked }` | `X-Member-UUID` 필수 |
| GET | `/api/v1/likes/{likeType}/{targetUuid}/count` | 대상 좋아요 수 | 없음 |
| GET | `/api/v1/likes/{likeType}/{targetUuid}` | 화면 스냅샷. `liked`, `likeCount`, `optimisticLikeCount`, `optimisticUnlikeCount` | `X-Member-UUID` 선택. 없으면 `liked=false` |
| POST | `/api/v1/likes/snapshots` | 댓글 목록 일괄 스냅샷. Body `{ likeType, targetUuids }`. 최대 50건 | `X-Member-UUID` 선택 |

### Deploy (인프라 확인용)

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/planwith-fo-like/deploy-check` | 배포 마커 확인 |
| POST | `/api/planwith-fo-like/login` | 로컬/배포 확인용 로그인 (`LOGIN_ID` / `LOGIN_PASSWORD`) |

### 오류 코드

| HTTP | code | 상황 |
|---|---|---|
| 400 | `INVALID_LIKE_TYPE` | `STORY`/`COMMENT` 이외 |
| 400 | `INVALID_LIKE_TARGET` | UUID 형식 오류, 스냅샷 대상 없음/50건 초과 |
| 400 | `INVALID_MEMBER` | 회원 식별자 없음 |
| 400 | `INVALID_REQUEST` | Bean Validation / 기타 헤더 |
| 400 | `DUPLICATE_LIKE` | 도메인 중복 (HTTP Command는 멱등으로 `alreadyApplied` 처리) |
| 401 | `AUTHENTICATION_REQUIRED` | Command·`/me`에 `X-Member-UUID` 없음 |
| 401 | `INVALID_CREDENTIALS` | Deploy 로그인 실패 |

---

## 4. 외부 연동

제3자 API는 없습니다. MSA/인프라 연동만 있습니다.

```
Gateway :8000
    ↓
planwith-fo-like :8091
    ↓
MySQL like_db
Redis (hot cache, 장애 시 MySQL 폴백)
    ↓
like_outbox → Relay → Kafka
    planwith.like.created
    planwith.like.removed
    ↓
Like Counter Consumer (inbox 멱등)
    ↓
like_target_counter ±1
```

| 대상 | 역할 |
|---|---|
| Eureka (`planwith-fo-like`) | 서비스 등록. `lb://planwith-fo-like` |
| Gateway | 외부 진입. 스니펫 기본 경로는 `/api/planwith-fo-like/**` |
| MySQL | `like_management`, `like_target_counter`, `like_outbox`, `like_count_inbox` |
| Redis | liked/count 캐시, 짧은 중복 가드. 최종 중복 방어는 UNIQUE |
| Kafka | LIKE/UNLIKE 발행·소비. Consumer 기본값 **비활성** |
| Story/Comment | 이벤트 구독·화면에서 Like Query 호출. 이 저장소에서 구현하지 않음 |
| Member | UUID만 헤더로 수신. Member DB 조회 없음 |

Kafka payload 주요 필드: `eventId`/`eventUuid`, `eventType`, `memberUuid`/`likerUuid`, `likeUuid`, `likeType`/`targetType`, `targetUuid`, `targetOwnerUuid`, `occurredAt`, `sourceVersion`

---

## 5. 비기능 / 품질

- Hexagonal: Controller → UseCase → Domain. Entity를 API로 반환하지 않음
- Command와 Query 분리. Counter 갱신은 비동기
- Outbox + Relay 재시도(최대 10회, backoff)로 Kafka 발행 실패 시 유실 완화
- Consumer inbox로 재처리 멱등
- Redis 장애/MISS 시 MySQL 폴백
- 동시 좋아요는 UNIQUE + 멱등. Redis 가드만으로 막지 않음
- 로그: `클래스 : 메서드 : 한글 역할`. 토큰·비밀번호 미출력
- 테스트: Unit / Integration / API / 전체 이벤트 흐름 E2E (`#3`~`#10`)
- Testcontainers MySQL 동시성 테스트는 `LIKE_CONCURRENCY_TEST=true`일 때만 실행

---

## 6. 배포 설정 요약

| 항목 | 값 |
|---|---|
| Compose / Eureka | `planwith-fo-like` |
| 이미지 | `planwith/planwith-fo-like:latest` |
| 포트 | `8091` (`127.0.0.1:8091:8091`) |
| Workflow | `develop` push 또는 PR merge 시 Self-hosted Runner |
| JPA | `ddl-auto=update` (운영 테이블은 기동 시 생성/갱신) |
| Health | `/actuator/health`, `/actuator/info` |
| Swagger | 로컬 `8091/swagger-ui.html`. Docker는 Gateway `:8000`에서 선택 |

주요 환경 변수:

| 변수 | 기본 / 의미 |
|---|---|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | `like_db` 접속. 로컬 호스트 포트 `3307` |
| `REDIS_HOST` / `REDIS_PORT` | 캐시 |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `LIKE_OUTBOX_ENABLED` | `true` |
| `LIKE_KAFKA_CONSUMER_ENABLED` | **`false`**. Counter 반영하려면 `true` |
| `LIKE_TOPIC_CREATED` | `planwith.like.created` |
| `LIKE_TOPIC_REMOVED` | `planwith.like.removed` |
| `KAFKA_CONSUMER_GROUP` | `like-service` |
| `EUREKA_CLIENT_ENABLED` | 서버 `true`, 로컬 예시는 `false` |
| `LIKE_CACHE_TTL` | `10m` |
| `DEPLOY_MARKER` | `planwith-fo-like-deploy-v1` |

로컬: `.env.example` + `.\gradlew.bat bootRun`  
서버: `env/like.env` + compose. 다른 PC는 서비스 포트를 직접 호출하지 않습니다.

---

## 7. 운영 주의사항

1. **Consumer 기본 꺼짐.** `LIKE_KAFKA_CONSUMER_ENABLED=true`와 Kafka/토픽이 있어야 DB Counter가 따라갑니다. 꺼져 있으면 HTTP `likeCount`는 Redis 낙관 값이고 `like_target_counter`는 비어 있을 수 있습니다.
2. **Outbox Relay**가 Kafka 발행을 담당합니다. Relay 실패는 재시도하고, 한도를 넘으면 로그로 남습니다. 미발행 Outbox를 주기적으로 확인해야 합니다.
3. **Gateway 경로.** 스니펫은 `/api/planwith-fo-like/**`입니다. 도메인 API는 `/api/v1/likes/**`이므로 Gateway에 해당 Path를 추가해야 `:8000`으로 화면 연동이 됩니다.
4. Story/Comment는 Like DB를 조회하지 말고 스냅샷/`/me`+`/count`를 호출합니다.
5. 프론트는 클릭 즉시 ±1, Command 실패(401/400) 시 이전 `likeCount`로 롤백합니다.
6. 일괄 스냅샷은 50건 초과 시 `INVALID_LIKE_TARGET`입니다.
7. Redis만으로 중복을 막았다고 보지 않습니다. 장애 시 MySQL UNIQUE가 최종 방어선입니다.
8. `ddl-auto=update`입니다. 스키마 변경은 배포 전 확인이 필요합니다.
9. Deploy 로그인(`test-001`)은 배포 확인용이며 회원 인증이 아닙니다. 좋아요 API는 `X-Member-UUID`입니다.

---

## 8. 개발 완료 범위 (단계 요약)

| STEP | 범위 | 상태 |
|---|---|---|
| 01 | Like Domain (`LikeManagement`, `LikeTargetCounter`, `LikeType`) | 완료 |
| 02 | 공통 Validation (회원/대상/타입/Soft Delete 상태) | 완료 |
| 03 | 좋아요 / 취소 / 재좋아요, UNIQUE 멱등 | 완료 |
| 04 | 내 좋아요 여부, 대상 수, Counter 조회, Redis 폴백 | 완료 |
| 05 | LIKE/UNLIKE 이벤트, Outbox payload | 완료 |
| 06 | Kafka 비동기 Counter, inbox 멱등, 0 바닥 | 완료 |
| 07 | Story/Comment 화면 스냅샷 + Optimistic UI 계약 | 완료 |
| 08 | 전체 이벤트 흐름 정합성 통합 테스트 | 완료 |

이슈 `#3` ~ `#10`에 해당합니다.

---

## 9. 검증 상태

- [x] Unit Test (도메인, Validator, Optimistic ±1, Event payload)
- [x] Integration Test (Command, Query, Outbox, Counter, Redis 폴백, 동시성)
- [x] API Test (MockMvc Command/Query/스냅샷)
- [x] E2E 정합성 (`LikeEndToEndConsistencyIntegrationTest`: LIKE→UNLIKE→LIKE, 타입 분리, Kafka 재처리, Optimistic UI, API 실패 Rollback)
- [x] `.\gradlew.bat test build` 성공

실제 Kafka 브로커 E2E와 Testcontainers 동시성은 환경 플래그/인프라가 있을 때 수행합니다. 화면 UI는 이 저장소에서 브라우저 검증하지 않았습니다.

---

**RM 결론:** `planwith-fo-like`는 Story/Comment 좋아요의 원장·카운트·이벤트를 독립 제공하는 서비스로, Command/Query/Outbox/비동기 Counter/화면 스냅샷까지 개발·테스트가 완료되었습니다. 운영 배포 시 **Kafka Consumer를 켜고**, Gateway에 **`/api/v1/likes/**` 경로를 연결하며**, Story/Comment/프론트는 Like DB가 아니라 본 API와 Optimistic UI 계약을 사용하면 됩니다.
