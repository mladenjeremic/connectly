package com.connectly.domain.model;

import com.connectly.domain.enums.ReportStatus;
import com.connectly.domain.enums.ReportTargetType;
import java.time.Instant;

public record Report(Long id, Long reporterId, ReportTargetType targetType, Long targetId, String category,
                     String description, ReportStatus status, Long reviewedBy, Instant reviewedAt, Instant createdAt) {

    public static Report create(Long reporterId, ReportTargetType type, Long targetId, String category, String description) {
        return new Report(null, reporterId, type, targetId, category,
                description == null ? "" : description.strip(), ReportStatus.PENDING, null, null, Instant.now());
    }

    public Report resolve(ReportStatus newStatus, Long adminId) {
        return new Report(id, reporterId, targetType, targetId, category, description,
                newStatus, adminId, Instant.now(), createdAt);
    }
}
