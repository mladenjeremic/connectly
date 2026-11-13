package com.connectly.domain.port;

import com.connectly.domain.model.Story;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StoryRepository {
    Story save(Story story);
    Optional<Story> findById(Long id);
    /** Priče koje još nisu istekle, starije prvo (redosled gledanja). */
    List<Story> findActiveByAuthors(Collection<Long> authorIds, Instant now);
    void delete(Long id);
}
