package com.connectly.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface BanJpaRepository extends JpaRepository<BanEntity, Long> {
    Optional<BanEntity> findFirstByUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId);
    List<BanEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<BanEntity> findByActiveTrueAndEndsAtIsNotNullAndEndsAtBefore(Instant now);

    @Modifying
    @Query("update BanEntity b set b.active = false where b.userId = :uid and b.active = true")
    void deactivateAll(@Param("uid") Long userId);
}
