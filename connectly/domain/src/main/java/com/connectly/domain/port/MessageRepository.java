package com.connectly.domain.port;

import com.connectly.domain.model.Message;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MessageRepository {
    Message save(Message message);
    /** Poruke razgovora, najnovije prve (stranica 0 = poslednjih nekoliko poruka). */
    List<Message> findNewest(Long conversationId, int page, int size);
    /** Nepročitane poruke koje je drugi učesnik poslao ovom korisniku, po razgovoru. */
    Map<Long, Long> countUnreadByConversation(Collection<Long> conversationIds, Long userId);
    long countUnreadTotal(Long userId);
    void markRead(Long conversationId, Long userId);
}
