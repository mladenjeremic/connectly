package com.connectly.infrastructure.persistence;

import com.connectly.domain.enums.AdminActionType;
import com.connectly.domain.model.AdminAction;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "admin_actions", indexes = @Index(columnList = "targetUserId"))
public class AdminActionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long adminId;
    public Long targetUserId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) public AdminActionType actionType;
    @Column(length = 500) public String reason;
    @Column(length = 500) public String metadata;
    public Instant createdAt;

    static AdminActionEntity from(AdminAction a) {
        AdminActionEntity e = new AdminActionEntity();
        e.id = a.id(); e.adminId = a.adminId(); e.targetUserId = a.targetUserId(); e.actionType = a.actionType();
        e.reason = a.reason(); e.metadata = a.metadata(); e.createdAt = a.createdAt();
        return e;
    }

    AdminAction toDomain() { return new AdminAction(id, adminId, targetUserId, actionType, reason, metadata, createdAt); }
}
