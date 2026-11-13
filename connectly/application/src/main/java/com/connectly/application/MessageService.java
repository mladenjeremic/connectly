package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.model.Conversation;
import com.connectly.domain.model.Message;
import com.connectly.domain.model.User;
import com.connectly.domain.port.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Direktne poruke između dva korisnika. Odgovori na priče stižu ovde preko {@link #sendStoryReply}. */
@Transactional
public class MessageService implements MessageOps {
    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final UserRepository users;
    private final FollowRepository follows;
    private final BlockRepository blocks;

    public MessageService(ConversationRepository conversations, MessageRepository messages,
                          UserRepository users, FollowRepository follows, BlockRepository blocks) {
        this.conversations = conversations; this.messages = messages; this.users = users; this.follows = follows;
        this.blocks = blocks;
    }

    @Override
    public MessageView send(Long senderId, Long recipientId, String content) {
        return toView(deliver(senderId, recipientId, content, null), senderId);
    }

    /** Poziva se kad neko odgovori na priču: odgovor postaje poruka u razgovoru sa autorom priče. */
    public void sendStoryReply(Long senderId, Long storyAuthorId, Long storyId, String content) {
        deliver(senderId, storyAuthorId, content, storyId);
    }

    private Message deliver(Long senderId, Long recipientId, String content, Long storyId) {
        if (senderId.equals(recipientId)) throw new DomainException("Ne možeš slati poruke sebi.");
        User recipient = users.findById(recipientId).orElseThrow(() -> new NotFoundException("Korisnik nije pronađen."));
        if (!recipient.isActive()) throw new DomainException("Ovom korisniku trenutno ne možeš slati poruke.");
        if (blocks.existsEither(senderId, recipientId)) throw new ForbiddenException("Ne možeš poslati poruku ovom korisniku.");

        Conversation conv = conversations.findBetween(senderId, recipientId)
                .orElseGet(() -> conversations.save(Conversation.between(senderId, recipientId)));
        Message saved = messages.save(Message.create(conv.id(), senderId, content, storyId));
        conversations.save(conv.withLastMessage(saved));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationView> conversations(Long userId, int page, int size) {
        List<Conversation> list = conversations.findByUser(userId, Math.max(0, page), Math.max(1, Math.min(size, 50)));
        if (list.isEmpty()) return List.of();

        List<Long> convIds = list.stream().map(Conversation::id).toList();
        List<Long> otherIds = list.stream().map(c -> c.other(userId)).toList();
        Map<Long, User> others = new HashMap<>();
        for (User u : users.findByIds(otherIds)) others.put(u.id(), u);
        Map<Long, Long> unread = messages.countUnreadByConversation(convIds, userId);
        Set<Long> followed = Set.copyOf(follows.findFollowedIds(userId));

        List<ConversationView> result = new ArrayList<>();
        for (Conversation c : list) {
            User other = others.get(c.other(userId));
            if (other == null) continue;
            UserSummary summary = new UserSummary(other.id(), other.username(), other.fullName(),
                    ImageRules.url(other.profileImage()), followed.contains(other.id()), false);
            result.add(new ConversationView(c.id(), summary, c.lastMessage(), userId.equals(c.lastSenderId()),
                    c.lastMessageAt(), unread.getOrDefault(c.id(), 0L)));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageView> thread(Long userId, Long otherUserId, int page, int size) {
        Optional<Conversation> conv = conversations.findBetween(userId, otherUserId);
        if (conv.isEmpty()) return List.of();
        List<Message> newestFirst = messages.findNewest(conv.get().id(), Math.max(0, page), Math.max(1, Math.min(size, 100)));
        List<MessageView> result = new ArrayList<>(newestFirst.stream().map(m -> toView(m, userId)).toList());
        Collections.reverse(result);   // u razgovoru najstarije ide gore
        return result;
    }

    @Override
    public void markRead(Long userId, Long otherUserId) {
        conversations.findBetween(userId, otherUserId).ifPresent(c -> messages.markRead(c.id(), userId));
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) { return messages.countUnreadTotal(userId); }

    private MessageView toView(Message m, Long viewerId) {
        return new MessageView(m.id(), m.senderId(), m.content(), m.senderId().equals(viewerId),
                m.storyId() != null, m.createdAt(), m.readAt() != null);
    }
}
