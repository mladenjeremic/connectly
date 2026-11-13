package com.connectly.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

interface MessageJpaRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findByConversationIdOrderByCreatedAtDescIdDesc(Long conversationId, Pageable pageable);

    @Query("select m.conversationId, count(m) from MessageEntity m where m.conversationId in :ids and m.senderId <> :uid and m.readAt is null group by m.conversationId")
    List<Object[]> countUnreadByConversation(@Param("ids") Collection<Long> ids, @Param("uid") Long userId);

    @Query("select count(m) from MessageEntity m where m.senderId <> :uid and m.readAt is null and m.conversationId in "
            + "(select c.id from ConversationEntity c where c.userLowId = :uid or c.userHighId = :uid)")
    long countUnreadTotal(@Param("uid") Long userId);

    @Modifying
    @Query("update MessageEntity m set m.readAt = :now where m.conversationId = :cid and m.senderId <> :uid and m.readAt is null")
    void markRead(@Param("cid") Long conversationId, @Param("uid") Long userId, @Param("now") Instant now);
}
