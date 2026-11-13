package com.connectly.infrastructure.persistence;

import com.connectly.domain.enums.ReportStatus;
import com.connectly.domain.enums.ReportTargetType;
import com.connectly.domain.model.Report;
import com.connectly.domain.port.ReportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Repository
class ReportRepositoryAdapter implements ReportRepository {
    private final ReportJpaRepository jpa;
    ReportRepositoryAdapter(ReportJpaRepository jpa) { this.jpa = jpa; }

    public Report save(Report r) { return jpa.save(ReportEntity.from(r)).toDomain(); }
    public Optional<Report> findById(Long id) { return jpa.findById(id).map(ReportEntity::toDomain); }

    public List<Report> findByStatus(ReportStatus status, int page, int size) {
        var statuses = status == null ? EnumSet.allOf(ReportStatus.class) : EnumSet.of(status);
        return jpa.findByStatuses(statuses, PageRequest.of(page, size)).stream().map(ReportEntity::toDomain).toList();
    }

    public long countByStatus(ReportStatus status) { return jpa.countByStatus(status); }

    public boolean existsPending(Long reporterId, ReportTargetType type, Long targetId) {
        return jpa.existsByReporterIdAndTargetTypeAndTargetIdAndStatus(reporterId, type, targetId, ReportStatus.PENDING);
    }

    public List<Report> findPendingByTarget(ReportTargetType type, Long targetId) {
        return jpa.findByTargetTypeAndTargetIdAndStatus(type, targetId, ReportStatus.PENDING)
                .stream().map(ReportEntity::toDomain).toList();
    }
}
