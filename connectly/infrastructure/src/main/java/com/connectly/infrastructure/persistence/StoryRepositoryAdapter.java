package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Story;
import com.connectly.domain.port.StoryRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
class StoryRepositoryAdapter implements StoryRepository {
    private final StoryJpaRepository jpa;
    StoryRepositoryAdapter(StoryJpaRepository jpa) { this.jpa = jpa; }

    public Story save(Story story) { return jpa.save(StoryEntity.from(story)).toDomain(); }
    public Optional<Story> findById(Long id) { return jpa.findById(id).map(StoryEntity::toDomain); }
    public List<Story> findActiveByAuthors(Collection<Long> authorIds, Instant now) {
        if (authorIds.isEmpty()) return List.of();
        return jpa.findActive(authorIds, now).stream().map(StoryEntity::toDomain).toList();
    }
    public void delete(Long id) { jpa.deleteById(id); }
}
