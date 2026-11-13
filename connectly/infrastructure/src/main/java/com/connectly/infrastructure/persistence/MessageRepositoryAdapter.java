package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Message;
import com.connectly.domain.port.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
class MessageRepositoryAdapter implements MessageRepository {
    private final MessageJpaRepository jpa;
    MessageRepositoryAdapter(MessageJpaRepository jpa) { this.jpa = jpa; }

    public Message save(Message m) { return jpa.save(MessageEntity.from(m)).toDomain(); }

    public List<Message> findNewest(Long conversationId, int page, int size) {
        return jpa.findByConversationIdOrderByCreatedAtDescIdDesc(conversationId, PageRequest.of(page, size))
                .stream().map(MessageEntity::toDomain).toList();
    }

    public Map<Long, Long> countUnreadByConversation(Collection<Long> conversationIds, Long userId) {
        Map<Long, Long> result = new HashMap<>();
        if (conversationIds.isEmpty()) return result;
        for (Object[] row : jpa.countUnreadByConversation(conversationIds, userId))
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        return result;
    }

    public long countUnreadTotal(Long userId) { return jpa.countUnreadTotal(userId); }

    public void markRead(Long conversationId, Long userId) { jpa.markRead(conversationId, userId, Instant.now()); }
}
