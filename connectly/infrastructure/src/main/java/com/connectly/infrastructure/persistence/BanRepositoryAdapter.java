package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Ban;
import com.connectly.domain.port.BanRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
class BanRepositoryAdapter implements BanRepository {
    private final BanJpaRepository jpa;
    BanRepositoryAdapter(BanJpaRepository jpa) { this.jpa = jpa; }

    public Ban save(Ban ban) { return jpa.save(BanEntity.from(ban)).toDomain(); }
    public Optional<Ban> findActiveByUserId(Long userId) {
        return jpa.findFirstByUserIdAndActiveTrueOrderByCreatedAtDesc(userId).map(BanEntity::toDomain);
    }
    public List<Ban> findByUserId(Long userId) {
        return jpa.findByUserIdOrderByCreatedAtDesc(userId).stream().map(BanEntity::toDomain).toList();
    }
    public List<Ban> findExpiredActive(Instant now) {
        return jpa.findByActiveTrueAndEndsAtIsNotNullAndEndsAtBefore(now).stream().map(BanEntity::toDomain).toList();
    }
    public void deactivateAllForUser(Long userId) { jpa.deactivateAll(userId); }
}
