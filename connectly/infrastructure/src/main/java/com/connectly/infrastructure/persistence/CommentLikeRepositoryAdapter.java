package com.connectly.infrastructure.persistence;
import com.connectly.domain.port.CommentLikeRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
class CommentLikeRepositoryAdapter implements CommentLikeRepository {
    private final CommentLikeJpaRepository jpa;
    CommentLikeRepositoryAdapter(CommentLikeJpaRepository jpa) { this.jpa = jpa; }

    public boolean exists(Long commentId, Long userId) { return jpa.existsByCommentIdAndUserId(commentId, userId); }
    public void add(Long commentId, Long userId) {
        CommentLikeEntity e = new CommentLikeEntity();
        e.commentId = commentId; e.userId = userId;
        jpa.save(e);
    }
    public void remove(Long commentId, Long userId) { jpa.deleteByCommentIdAndUserId(commentId, userId); }
    public long count(Long commentId) { return jpa.countByCommentId(commentId); }

    public Map<Long, Long> countByComments(Collection<Long> commentIds) {
        Map<Long, Long> result = new HashMap<>();
        if (commentIds.isEmpty()) return result;
        for (Object[] row : jpa.countByComments(commentIds))
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        return result;
    }
    public Set<Long> likedBy(Collection<Long> commentIds, Long userId) {
        if (commentIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(jpa.likedBy(commentIds, userId));
    }
}
