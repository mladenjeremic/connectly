package com.connectly.infrastructure.persistence;

import com.connectly.domain.enums.ReportStatus;
import com.connectly.domain.enums.ReportTargetType;
import com.connectly.domain.model.Report;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "reports", indexes = @Index(columnList = "status"))
public class ReportEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long reporterId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public ReportTargetType targetType;
    @Column(nullable = false) public Long targetId;
    @Column(nullable = false, length = 30) public String category;
    @Column(length = 1000) public String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public ReportStatus status;
    public Long reviewedBy;
    public Instant reviewedAt;
    public Instant createdAt;

    static ReportEntity from(Report r) {
        ReportEntity e = new ReportEntity();
        e.id = r.id(); e.reporterId = r.reporterId(); e.targetType = r.targetType(); e.targetId = r.targetId();
        e.category = r.category(); e.description = r.description(); e.status = r.status();
        e.reviewedBy = r.reviewedBy(); e.reviewedAt = r.reviewedAt(); e.createdAt = r.createdAt();
        return e;
    }

    Report toDomain() {
        return new Report(id, reporterId, targetType, targetId, category, description == null ? "" : description,
                status, reviewedBy, reviewedAt, createdAt);
    }
}
