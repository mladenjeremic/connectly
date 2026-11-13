package com.connectly.domain.port;

import com.connectly.domain.enums.ReportStatus;
import com.connectly.domain.enums.ReportTargetType;
import com.connectly.domain.model.Report;
import java.util.List;
import java.util.Optional;

public interface ReportRepository {
    Report save(Report report);
    Optional<Report> findById(Long id);
    /** status == null vraća prijave sa bilo kojim statusom. */
    List<Report> findByStatus(ReportStatus status, int page, int size);
    long countByStatus(ReportStatus status);
    boolean existsPending(Long reporterId, ReportTargetType type, Long targetId);
    /** Sve nerešene prijave na isti sadržaj (za zatvaranje kad se sadržaj ukloni). */
    List<Report> findPendingByTarget(ReportTargetType type, Long targetId);
}
