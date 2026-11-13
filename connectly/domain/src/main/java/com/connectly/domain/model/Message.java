package com.connectly.domain.model;

import com.connectly.domain.exception.DomainException;
import java.time.Instant;

/** Poruka u razgovoru. {@code storyId} je popunjen kad je poruka odgovor na priču. */
public record Message(Long id, Long conversationId, Long senderId, String content, Long storyId,
                      Instant createdAt, Instant readAt) {
    public static final int MAX_LENGTH = 2000;

    public Message {
        if (content == null || content.isBlank() || content.length() > MAX_LENGTH)
            throw new DomainException("Poruka mora imati 1-" + MAX_LENGTH + " karaktera.");
    }

    public static Message create(Long conversationId, Long senderId, String content, Long storyId) {
        if (content == null) throw new DomainException("Poruka mora imati 1-" + MAX_LENGTH + " karaktera.");
        return new Message(null, conversationId, senderId, content.strip(), storyId, Instant.now(), null);
    }
}
