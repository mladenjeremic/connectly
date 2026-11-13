package com.connectly.domain.model;

import com.connectly.domain.exception.DomainException;
import java.time.Instant;

/**
 * Razgovor između tačno dva korisnika. Učesnici se čuvaju uređeno (manji id prvi), pa za par postoji
 * najviše jedan razgovor. Poslednja poruka je u razgovoru, da se lista razgovora učita bez dodatnih upita.
 */
public record Conversation(Long id, Long userLowId, Long userHighId, String lastMessage, Long lastSenderId,
                           Instant lastMessageAt) {
    public static final int PREVIEW_LENGTH = 120;

    public Conversation {
        if (userLowId == null || userHighId == null || userLowId.equals(userHighId) || userLowId > userHighId)
            throw new DomainException("Razgovor mora imati dva različita učesnika.");
    }

    public static Conversation between(Long a, Long b) {
        if (a == null || b == null || a.equals(b)) throw new DomainException("Ne možeš slati poruke sebi.");
        return new Conversation(null, Math.min(a, b), Math.max(a, b), "", null, Instant.now());
    }

    public boolean hasParticipant(Long userId) {
        return userLowId.equals(userId) || userHighId.equals(userId);
    }

    public Long other(Long userId) {
        return userLowId.equals(userId) ? userHighId : userLowId;
    }

    public Conversation withLastMessage(Message m) {
        String text = m.content().length() > PREVIEW_LENGTH ? m.content().substring(0, PREVIEW_LENGTH) : m.content();
        return new Conversation(id, userLowId, userHighId, text, m.senderId(), m.createdAt());
    }
}
