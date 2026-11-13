package com.connectly.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

interface StoryJpaRepository extends JpaRepository<StoryEntity, Long> {
    @Query("select s from StoryEntity s where s.authorId in :ids and s.expiresAt > :now order by s.createdAt asc")
    List<StoryEntity> findActive(@Param("ids") Collection<Long> ids, @Param("now") Instant now);
}
