package com.connectly.infrastructure.persistence;
import com.connectly.domain.port.BookmarkRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
class BookmarkRepositoryAdapter implements BookmarkRepository {
    private final BookmarkJpaRepository jpa;
    BookmarkRepositoryAdapter(BookmarkJpaRepository jpa) { this.jpa = jpa; }

    public boolean exists(Long postId, Long userId) { return jpa.existsByPostIdAndUserId(postId, userId); }
    public void add(Long postId, Long userId) {
        BookmarkEntity e = new BookmarkEntity();
        e.postId = postId; e.userId = userId;
        jpa.save(e);
    }
    public void remove(Long postId, Long userId) { jpa.deleteByPostIdAndUserId(postId, userId); }
    public Set<Long> savedAmong(Collection<Long> postIds, Long userId) {
        if (postIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(jpa.savedAmong(postIds, userId));
    }
    public List<Long> findPostIds(Long userId, int page, int size) {
        return jpa.findPostIds(userId, PageRequest.of(page, size));
    }
}
