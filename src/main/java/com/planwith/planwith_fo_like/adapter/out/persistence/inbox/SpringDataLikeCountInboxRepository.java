package com.planwith.planwith_fo_like.adapter.out.persistence.inbox;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLikeCountInboxRepository extends JpaRepository<LikeCountInboxJpaEntity, UUID> {
}
