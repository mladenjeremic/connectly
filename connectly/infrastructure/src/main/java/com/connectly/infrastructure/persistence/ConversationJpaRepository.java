package com.connectly.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

interface ConversationJpaRepository extends JpaRepository<ConversationEntity, Long> {
    Optional<ConversationEntity> findByUserLowIdAndUserHighId(Long low, Long high);

    @Query("select c from ConversationEntity c where (c.userLowId = :uid or c.userHighId = :uid) and c.lastMessageAt is not null order by c.lastMessageAt desc")
    List<ConversationEntity> findByUser(@Param("uid") Long userId, Pageable pageable);
}
