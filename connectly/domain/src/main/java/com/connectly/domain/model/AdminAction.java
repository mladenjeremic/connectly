package com.connectly.domain.model;

import com.connectly.domain.enums.AdminActionType;
import java.time.Instant;

public record AdminAction(Long id, Long adminId, Long targetUserId, AdminActionType actionType,
                          String reason, String metadata, Instant createdAt) {
    public static AdminAction of(Long adminId, Long targetUserId, AdminActionType type, String reason, String metadata) {
        return new AdminAction(null, adminId, targetUserId, type, reason, metadata, Instant.now());
    }
}
