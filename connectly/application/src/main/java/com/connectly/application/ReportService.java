package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.enums.ReportStatus;
import com.connectly.domain.enums.ReportTargetType;
import com.connectly.domain.exception.ConflictException;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.model.Comment;
import com.connectly.domain.model.Post;
import com.connectly.domain.model.Report;
import com.connectly.domain.port.CommentRepository;
import com.connectly.domain.port.PostRepository;
import com.connectly.domain.port.ReportRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Transactional
public class ReportService implements ReportOps {
    public static final Set<String> CATEGORIES =
            Set.of("SPAM", "HARASSMENT", "HATE_SPEECH", "VIOLENCE", "NUDITY", "MISINFORMATION", "OTHER");

    private final ReportRepository reports;
    private final PostRepository posts;
    private final CommentRepository comments;

    public ReportService(ReportRepository reports, PostRepository posts, CommentRepository comments) {
        this.reports = reports; this.posts = posts; this.comments = comments;
    }

    @Override
    public ReportReceipt create(Long reporterId, ReportTargetType type, Long targetId, String category, String description) {
        if (category == null || !CATEGORIES.contains(category)) throw new DomainException("Nepoznata kategorija prijave.");
        if (description != null && description.length() > 1000)
            throw new DomainException("Opis može imati najviše 1000 karaktera.");

        Long authorId;
        if (type == ReportTargetType.POST) {
            Post p = posts.findById(targetId).orElseThrow(() -> new NotFoundException("Objava nije pronađena."));
            authorId = p.authorId();
        } else {
            Comment c = comments.findById(targetId).orElseThrow(() -> new NotFoundException("Komentar nije pronađen."));
            authorId = c.authorId();
        }
        if (authorId.equals(reporterId)) throw new DomainException("Ne možeš prijaviti sopstveni sadržaj.");
        if (reports.existsPending(reporterId, type, targetId))
            throw new ConflictException("Već si prijavio ovaj sadržaj, prijava čeka obradu.");

        Report saved = reports.save(Report.create(reporterId, type, targetId, category, description));
        return new ReportReceipt(saved.id(), ReportStatus.PENDING);
    }
}
