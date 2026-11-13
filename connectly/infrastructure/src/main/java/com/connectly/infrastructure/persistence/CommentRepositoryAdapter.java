package com.connectly.infrastructure.persistence;
import com.connectly.domain.model.Comment;
import com.connectly.domain.port.CommentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
class CommentRepositoryAdapter implements CommentRepository {
    private final CommentJpaRepository jpa;
    CommentRepositoryAdapter(CommentJpaRepository jpa) { this.jpa = jpa; }

    public Comment save(Comment c) { return jpa.save(CommentEntity.from(c)).toDomain(); }
    public Optional<Comment> findById(Long id) {
        return jpa.findById(id).filter(e -> e.deletedAt == null).map(CommentEntity::toDomain);
    }
    public List<Comment> findByPostId(Long postId, int page, int size) {
        return jpa.findByPostIdAndParentIdIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(postId, PageRequest.of(page, size))
                .stream().map(CommentEntity::toDomain).toList();
    }
    public List<Comment> findReplies(Long parentId, int page, int size) {
        return jpa.findByParentIdAndDeletedAtIsNullOrderByCreatedAtAsc(parentId, PageRequest.of(page, size))
                .stream().map(CommentEntity::toDomain).toList();
    }
    public Map<Long, Long> countRepliesByParents(Collection<Long> parentIds) {
        Map<Long, Long> result = new HashMap<>();
        if (parentIds.isEmpty()) return result;
        for (Object[] row : jpa.countReplies(parentIds))
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        return result;
    }
    public long count(Long postId) { return jpa.countByPostIdAndDeletedAtIsNull(postId); }
    public void softDelete(Long id) {
        Instant now = Instant.now();
        jpa.findById(id).ifPresent(e -> { e.deletedAt = now; jpa.save(e); });
        jpa.softDeleteReplies(id, now);   // brisanjem korena nestaje cela nit
    }
}
