package com.connectly.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface AdminActionJpaRepository extends JpaRepository<AdminActionEntity, Long> {
    List<AdminActionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<AdminActionEntity> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId, Pageable pageable);
}
