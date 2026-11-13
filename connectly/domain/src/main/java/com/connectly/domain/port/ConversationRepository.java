package com.connectly.domain.port;

import com.connectly.domain.model.Conversation;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findBetween(Long userA, Long userB);
    /** Razgovori korisnika, sa najnovijom porukom prvi. */
    List<Conversation> findByUser(Long userId, int page, int size);
}
