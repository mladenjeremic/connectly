package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.AdminActionType;
import com.connectly.domain.enums.ReportStatus;
import com.connectly.domain.enums.ReportTargetType;
import com.connectly.domain.event.AccountStatusChanged;
import com.connectly.domain.exception.ConflictException;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.model.*;
import com.connectly.domain.port.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Transactional
public class ModerationService implements ModerationOps {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    private final UserRepository users;
    private final PostRepository posts;
    private final PostImageRepository postImages;
    private final CommentRepository comments;
    private final ReportRepository reports;
    private final BanRepository bans;
    private final AdminActionRepository actions;
    private final EventPublisher events;

    public ModerationService(UserRepository users, PostRepository posts, PostImageRepository postImages,
                             CommentRepository comments, ReportRepository reports, BanRepository bans,
                             AdminActionRepository actions, EventPublisher events) {
        this.users = users; this.posts = posts; this.postImages = postImages; this.comments = comments;
        this.reports = reports; this.bans = bans; this.actions = actions; this.events = events;
    }

    // ---------- dashboard i korisnici ----------

    @Override
    @Transactional(readOnly = true)
    public DashboardView dashboard() {
        return new DashboardView(users.count(), users.countByStatus(AccountStatus.ACTIVE),
                users.countByStatus(AccountStatus.SUSPENDED), users.countByStatus(AccountStatus.BANNED),
                posts.count(), reports.countByStatus(ReportStatus.PENDING));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserView> users(String query, AccountStatus status, int page, int size) {
        var statuses = status == null ? EnumSet.allOf(AccountStatus.class) : EnumSet.of(status);
        String q = query == null ? "" : query.strip();
        return users.searchAdmin(q, statuses, Math.max(0, page), Math.max(1, Math.min(size, 100)))
                .stream().map(this::toAdminView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetail userDetail(Long userId) {
        User u = requireUser(userId);
        List<BanView> banViews = bans.findByUserId(userId).stream().map(this::toBanView).toList();
        List<AdminActionView> history = actions.findByTargetUser(userId, 0, 50).stream().map(this::toActionView).toList();
        return new AdminUserDetail(toAdminView(u), banViews, history);
    }

    // ---------- zabrane ----------

    @Override
    public AdminUserView ban(Long adminId, Long userId, int days, String reason) {
        String why = requireReason(reason);
        if (days < 1 || days > 365) throw new DomainException("Trajanje zabrane mora biti između 1 i 365 dana.");
        User target = requireManageable(adminId, userId);
        if (target.status() == AccountStatus.BANNED) throw new ConflictException("Nalog je već trajno blokiran.");

        bans.deactivateAllForUser(userId);
        Ban ban = bans.save(Ban.temporary(userId, adminId, why, Duration.ofDays(days)));
        User saved = users.save(target.withStatus(AccountStatus.SUSPENDED));
        actions.save(AdminAction.of(adminId, userId, AdminActionType.BAN, why, "days=" + days));
        events.publish(new AccountStatusChanged(userId, "Tvoj nalog je suspendovan do "
                + FMT.format(ban.endsAt()) + ". Razlog: " + why));
        return toAdminView(saved);
    }

    @Override
    public AdminUserView permanentBan(Long adminId, Long userId, String reason) {
        String why = requireReason(reason);
        User target = requireManageable(adminId, userId);
        if (target.status() == AccountStatus.BANNED) throw new ConflictException("Nalog je već trajno blokiran.");

        bans.deactivateAllForUser(userId);
        bans.save(Ban.permanent(userId, adminId, why));
        User saved = users.save(target.withStatus(AccountStatus.BANNED));
        actions.save(AdminAction.of(adminId, userId, AdminActionType.PERMANENT_BAN, why, "permanent=true"));
        events.publish(new AccountStatusChanged(userId, "Tvoj nalog je trajno blokiran. Razlog: " + why));
        return toAdminView(saved);
    }

    @Override
    public AdminUserView unban(Long adminId, Long userId, String reason) {
        String why = requireReason(reason);
        User target = requireManageable(adminId, userId);
        if (target.status() != AccountStatus.SUSPENDED && target.status() != AccountStatus.BANNED)
            throw new DomainException("Nalog nije blokiran.");

        bans.deactivateAllForUser(userId);
        User saved = users.save(target.withStatus(AccountStatus.ACTIVE));
        actions.save(AdminAction.of(adminId, userId, AdminActionType.UNBAN, why, null));
        events.publish(new AccountStatusChanged(userId, "Zabrana na tvom nalogu je skinuta."));
        return toAdminView(saved);
    }

    @Override
    public AdminUserView deactivate(Long adminId, Long userId, String reason) {
        String why = requireReason(reason);
        User target = requireManageable(adminId, userId);
        if (target.status() == AccountStatus.DEACTIVATED) throw new ConflictException("Nalog je već deaktiviran.");

        bans.deactivateAllForUser(userId);
        User saved = users.save(target.withStatus(AccountStatus.DEACTIVATED));
        actions.save(AdminAction.of(adminId, userId, AdminActionType.DEACTIVATE, why, null));
        events.publish(new AccountStatusChanged(userId, "Tvoj nalog je deaktiviran. Razlog: " + why));
        return toAdminView(saved);
    }

    @Override
    public AdminUserView reactivate(Long adminId, Long userId, String reason) {
        String why = requireReason(reason);
        User target = requireManageable(adminId, userId);
        if (target.status() != AccountStatus.DEACTIVATED) throw new DomainException("Nalog nije deaktiviran.");

        User saved = users.save(target.withStatus(AccountStatus.ACTIVE));
        actions.save(AdminAction.of(adminId, userId, AdminActionType.REACTIVATE, why, null));
        events.publish(new AccountStatusChanged(userId, "Tvoj nalog je ponovo aktiviran."));
        return toAdminView(saved);
    }

    @Override
    public int expireBans() {
        List<Ban> expired = bans.findExpiredActive(Instant.now());
        for (Ban ban : expired) {
            bans.deactivateAllForUser(ban.userId());
            Optional<User> u = users.findById(ban.userId());
            if (u.isPresent() && u.get().status() == AccountStatus.SUSPENDED) {
                users.save(u.get().withStatus(AccountStatus.ACTIVE));
                events.publish(new AccountStatusChanged(ban.userId(), "Suspenzija tvog naloga je istekla."));
            }
        }
        return expired.size();
    }

    // ---------- sadržaj i prijave ----------

    @Override
    public void removePost(Long adminId, Long postId, String reason) {
        String why = requireReason(reason);
        Post p = posts.findById(postId).orElseThrow(() -> new NotFoundException("Objava nije pronađena."));
        posts.softDelete(postId);
        actions.save(AdminAction.of(adminId, p.authorId(), AdminActionType.REMOVE_POST, why, "postId=" + postId));
        closePending(ReportTargetType.POST, postId, adminId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportView> reports(ReportStatus status, int page, int size) {
        return reports.findByStatus(status, Math.max(0, page), Math.max(1, Math.min(size, 50)))
                .stream().map(this::toReportView).toList();
    }

    @Override
    public ReportView handleReport(Long adminId, Long reportId, ReportDecision decision, String note) {
        Report r = reports.findById(reportId).orElseThrow(() -> new NotFoundException("Prijava nije pronađena."));
        if (r.status() != ReportStatus.PENDING) throw new ConflictException("Prijava je već obrađena.");
        String why = note == null || note.isBlank() ? "Obrada prijave #" + reportId : note.strip();

        switch (decision) {
            case DISMISS -> {
                reports.save(r.resolve(ReportStatus.DISMISSED, adminId));
                actions.save(AdminAction.of(adminId, authorOf(r), AdminActionType.REPORT_DISMISSED, why, "reportId=" + reportId));
            }
            case REVIEW -> {
                reports.save(r.resolve(ReportStatus.REVIEWED, adminId));
                actions.save(AdminAction.of(adminId, authorOf(r), AdminActionType.REPORT_REVIEWED, why, "reportId=" + reportId));
            }
            case REMOVE_CONTENT -> {
                Long author = authorOf(r);
                if (r.targetType() == ReportTargetType.POST) {
                    posts.softDelete(r.targetId());
                    actions.save(AdminAction.of(adminId, author, AdminActionType.REMOVE_POST, why, "postId=" + r.targetId() + ",reportId=" + reportId));
                } else {
                    comments.softDelete(r.targetId());
                    actions.save(AdminAction.of(adminId, author, AdminActionType.REMOVE_COMMENT, why, "commentId=" + r.targetId() + ",reportId=" + reportId));
                }
                closePending(r.targetType(), r.targetId(), adminId);
            }
        }
        return toReportView(reports.findById(reportId).orElse(r));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminActionView> auditLog(int page, int size) {
        return actions.findAll(Math.max(0, page), Math.max(1, Math.min(size, 100))).stream().map(this::toActionView).toList();
    }

    // ---------- pomoćne ----------

    private void closePending(ReportTargetType type, Long targetId, Long adminId) {
        for (Report pending : reports.findPendingByTarget(type, targetId))
            reports.save(pending.resolve(ReportStatus.REVIEWED, adminId));
    }

    private Long authorOf(Report r) {
        if (r.targetType() == ReportTargetType.POST) return posts.findById(r.targetId()).map(Post::authorId).orElse(null);
        return comments.findById(r.targetId()).map(Comment::authorId).orElse(null);
    }

    private User requireUser(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("Korisnik nije pronađen."));
    }

    /** Admin ne sme da moderira sebe niti drugog administratora. */
    private User requireManageable(Long adminId, Long userId) {
        User target = requireUser(userId);
        if (target.id().equals(adminId)) throw new ForbiddenException("Ne možeš sprovesti akciju nad sopstvenim nalogom.");
        if (target.isAdmin()) throw new ForbiddenException("Ne možeš moderirati drugog administratora.");
        return target;
    }

    private static String requireReason(String reason) {
        if (reason == null || reason.isBlank()) throw new DomainException("Razlog je obavezan.");
        if (reason.strip().length() > 500) throw new DomainException("Razlog može imati najviše 500 karaktera.");
        return reason.strip();
    }

    private String usernameOf(Long id) {
        if (id == null) return null;
        return users.findById(id).map(User::username).orElse("deleted user");
    }

    private AdminUserView toAdminView(User u) {
        BanView activeBan = null;
        if (u.status() == AccountStatus.SUSPENDED || u.status() == AccountStatus.BANNED)
            activeBan = bans.findActiveByUserId(u.id()).map(this::toBanView).orElse(null);
        return new AdminUserView(u.id(), u.firstName(), u.lastName(), u.username(), u.email(),
                ImageRules.url(u.profileImage()), u.role(), u.status(), u.createdAt(),
                posts.countByAuthor(u.id()), activeBan);
    }

    private BanView toBanView(Ban b) {
        return new BanView(b.id(), b.reason(), b.startsAt(), b.endsAt(), b.active(), b.isPermanent(), usernameOf(b.adminId()));
    }

    private AdminActionView toActionView(AdminAction a) {
        return new AdminActionView(a.id(), a.adminId(), usernameOf(a.adminId()), a.targetUserId(),
                usernameOf(a.targetUserId()), a.actionType(), a.reason(), a.metadata(), a.createdAt());
    }

    private ReportView toReportView(Report r) {
        String preview = null, imageUrl = null;
        Long postId = null, authorId = null;
        boolean available = false;
        if (r.targetType() == ReportTargetType.POST) {
            Optional<Post> p = posts.findById(r.targetId());
            if (p.isPresent()) {
                available = true; postId = p.get().id(); authorId = p.get().authorId();
                preview = snippet(p.get().content());
                List<PostImage> imgs = new ArrayList<>(postImages.findByPostIds(List.of(p.get().id())));
                imgs.sort((a, b) -> Integer.compare(a.sortOrder(), b.sortOrder()));
                if (!imgs.isEmpty()) imageUrl = ImageRules.url(imgs.get(0).fileName());
            }
        } else {
            Optional<Comment> c = comments.findById(r.targetId());
            if (c.isPresent()) {
                available = true; postId = c.get().postId(); authorId = c.get().authorId();
                preview = snippet(c.get().content());
            }
        }
        return new ReportView(r.id(), r.reporterId(), usernameOf(r.reporterId()), r.targetType(), r.targetId(),
                postId, authorId, usernameOf(authorId), preview, imageUrl, available, r.category(),
                r.description(), r.status(), usernameOf(r.reviewedBy()), r.reviewedAt(), r.createdAt());
    }

    private static String snippet(String s) {
        if (s == null) return "";
        return s.length() <= 200 ? s : s.substring(0, 200) + "…";
    }
}
