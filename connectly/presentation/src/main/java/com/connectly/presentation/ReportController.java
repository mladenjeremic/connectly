package com.connectly.presentation;

import com.connectly.application.UseCases.*;
import com.connectly.domain.enums.ReportTargetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
class ReportController {

    record CreateReportRequest(@NotNull ReportTargetType targetType, @NotNull Long targetId,
                               @NotBlank String category, @Size(max = 1000) String description) {}

    private final ReportOps reports;
    ReportController(ReportOps reports) { this.reports = reports; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    ReportReceipt create(@AuthenticationPrincipal Long userId, @Valid @RequestBody CreateReportRequest r) {
        return reports.create(userId, r.targetType(), r.targetId(), r.category(), r.description());
    }
}
