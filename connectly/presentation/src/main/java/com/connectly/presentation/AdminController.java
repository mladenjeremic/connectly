package com.connectly.presentation;

import com.connectly.application.UseCases.*;
import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.ReportStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Svi endpoint-i ovde su zaštićeni u SecurityConfig-u (hasRole ADMIN), a ne samo sakriveni u React-u. */
@RestController
@RequestMapping("/api/admin")
class AdminController {

    record BanRequest(@Min(1) @Max(365) int days, @NotBlank @Size(max = 500) String reason) {}
    record ReasonRequest(@NotBlank @Size(max = 500) String reason) {}
    record ReportDecisionRequest(@NotNull ReportDecision decision, @Size(max = 500) String note) {}

    private final ModerationOps moderation;
    private final GetFeed feed;

    AdminController(ModerationOps moderation, GetFeed feed) { this.moderation = moderation; this.feed = feed; }

    @GetMapping("/dashboard")
    DashboardView dashboard() { return moderation.dashboard(); }

    @GetMapping("/users")
    List<AdminUserView> users(@RequestParam(defaultValue = "") String q,
                              @RequestParam(required = false) AccountStatus status,
                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return moderation.users(q, status, page, size);
    }

    @GetMapping("/users/{id}")
    AdminUserDetail user(@PathVariable Long id) { return moderation.userDetail(id); }

    @PostMapping("/users/{id}/ban")
    AdminUserView ban(@AuthenticationPrincipal Long adminId, @PathVariable Long id, @Valid @RequestBody BanRequest r) {
        return moderation.ban(adminId, id, r.days(), r.reason());
    }

    @PostMapping("/users/{id}/unban")
    AdminUserView unban(@AuthenticationPrincipal Long adminId, @PathVariable Long id, @Valid @RequestBody ReasonRequest r) {
        return moderation.unban(adminId, id, r.reason());
    }

    @PostMapping("/users/{id}/permanent-ban")
    AdminUserView permanentBan(@AuthenticationPrincipal Long adminId, @PathVariable Long id, @Valid @RequestBody ReasonRequest r) {
        return moderation.permanentBan(adminId, id, r.reason());
    }

    @PostMapping("/users/{id}/deactivate")
    AdminUserView deactivate(@AuthenticationPrincipal Long adminId, @PathVariable Long id, @Valid @RequestBody ReasonRequest r) {
        return moderation.deactivate(adminId, id, r.reason());
    }

    @PostMapping("/users/{id}/reactivate")
    AdminUserView reactivate(@AuthenticationPrincipal Long adminId, @PathVariable Long id, @Valid @RequestBody ReasonRequest r) {
        return moderation.reactivate(adminId, id, r.reason());
    }

    @GetMapping("/posts")
    List<PostView> posts(@AuthenticationPrincipal Long adminId,
                         @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return feed.feed(adminId, page, size);
    }

    @DeleteMapping("/posts/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void removePost(@AuthenticationPrincipal Long adminId, @PathVariable Long id, @Valid @RequestBody ReasonRequest r) {
        moderation.removePost(adminId, id, r.reason());
    }

    @GetMapping("/reports")
    List<ReportView> reports(@RequestParam(required = false) ReportStatus status,
                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return moderation.reports(status, page, size);
    }

    @PatchMapping("/reports/{id}")
    ReportView handleReport(@AuthenticationPrincipal Long adminId, @PathVariable Long id,
                            @Valid @RequestBody ReportDecisionRequest r) {
        return moderation.handleReport(adminId, id, r.decision(), r.note());
    }

    @GetMapping("/audit-log")
    List<AdminActionView> auditLog(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size) {
        return moderation.auditLog(page, size);
    }
}
