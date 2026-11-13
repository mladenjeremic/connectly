package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Post;
import com.connectly.domain.port.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
class PostRepositoryAdapter implements PostRepository {
    private final PostJpaRepository jpa;
    PostRepositoryAdapter(PostJpaRepository jpa) { this.jpa = jpa; }

    /** Pri izmeni se učitava postojeći red, da se ne bi izgubio deletedAt i slična polja. */
    public Post save(Post p) {
        PostEntity e = p.id() == null ? new PostEntity() : jpa.findById(p.id()).orElseGet(PostEntity::new);
        e.authorId = p.authorId();
        e.content = p.content();
        e.createdAt = p.createdAt();
        e.updatedAt = p.updatedAt();
        return jpa.save(e).toDomain();
    }

    public Optional<Post> findById(Long id) {
        return jpa.findById(id).filter(e -> e.deletedAt == null).map(PostEntity::toDomain);
    }

    public List<Post> findLatest(int page, int size) {
        return jpa.findLatest(PageRequest.of(page, size)).stream().map(PostEntity::toDomain).toList();
    }

    public List<Post> findByAuthors(Collection<Long> authorIds, int page, int size) {
        if (authorIds.isEmpty()) return List.of();
        return jpa.findByAuthors(authorIds, PageRequest.of(page, size)).stream().map(PostEntity::toDomain).toList();
    }

    public void softDelete(Long id) {
        jpa.findById(id).ifPresent(e -> { e.deletedAt = Instant.now(); jpa.save(e); });
    }

    public long count() { return jpa.countByDeletedAtIsNull(); }
    public long countByAuthor(Long authorId) { return jpa.countByAuthorIdAndDeletedAtIsNull(authorId); }
}
