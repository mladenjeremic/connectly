package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Conversation;
import com.connectly.domain.port.ConversationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
class ConversationRepositoryAdapter implements ConversationRepository {
    private final ConversationJpaRepository jpa;
    ConversationRepositoryAdapter(ConversationJpaRepository jpa) { this.jpa = jpa; }

    public Conversation save(Conversation c) { return jpa.save(ConversationEntity.from(c)).toDomain(); }

    public Optional<Conversation> findBetween(Long a, Long b) {
        return jpa.findByUserLowIdAndUserHighId(Math.min(a, b), Math.max(a, b)).map(ConversationEntity::toDomain);
    }

    public List<Conversation> findByUser(Long userId, int page, int size) {
        return jpa.findByUser(userId, PageRequest.of(page, size)).stream().map(ConversationEntity::toDomain).toList();
    }
}
