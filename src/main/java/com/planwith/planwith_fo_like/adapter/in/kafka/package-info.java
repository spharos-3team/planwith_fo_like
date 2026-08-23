/**
 * 좋아요 서비스 inbound Kafka 경계.
 * 이번 작업의 출력은 LikeCreated/LikeRemoved 발행이며,
 * Story/Comment 삭제 이벤트 수신 정리는 후속 이슈에서 이 패키지에 추가한다.
 * 소비 후보: Story, Comment, Grade, Notification.
 */
package com.planwith.planwith_fo_like.adapter.in.kafka;
