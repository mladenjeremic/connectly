package com.connectly.infrastructure.persistence;

import com.connectly.domain.enums.ReportStatus;
import com.connectly.domain.enums.ReportTargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

interface ReportJpaRepository extends JpaRepository<ReportEntity, Long> {
    @Query("select r from ReportEntity r where r.status in :statuses order by r.createdAt desc")
    List<ReportEntity> findByStatuses(@Param("statuses") Collection<ReportStatus> statuses, Pageable pageable);

    long countByStatus(ReportStatus status);
    boolean existsByReporterIdAndTargetTypeAndTargetIdAndStatus(Long reporterId, ReportTargetType type, Long targetId, ReportStatus status);
    List<ReportEntity> findByTargetTypeAndTargetIdAndStatus(ReportTargetType type, Long targetId, ReportStatus status);
}
