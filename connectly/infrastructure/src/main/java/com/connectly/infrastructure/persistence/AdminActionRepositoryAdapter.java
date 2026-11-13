package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.AdminAction;
import com.connectly.domain.port.AdminActionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
class AdminActionRepositoryAdapter implements AdminActionRepository {
    private final AdminActionJpaRepository jpa;
    AdminActionRepositoryAdapter(AdminActionJpaRepository jpa) { this.jpa = jpa; }

    public AdminAction save(AdminAction action) { return jpa.save(AdminActionEntity.from(action)).toDomain(); }
    public List<AdminAction> findAll(int page, int size) {
        return jpa.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).stream().map(AdminActionEntity::toDomain).toList();
    }
    public List<AdminAction> findByTargetUser(Long targetUserId, int page, int size) {
        return jpa.findByTargetUserIdOrderByCreatedAtDesc(targetUserId, PageRequest.of(page, size))
                .stream().map(AdminActionEntity::toDomain).toList();
    }
}
