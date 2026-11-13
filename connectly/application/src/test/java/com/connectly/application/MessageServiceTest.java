package com.connectly.application;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.Role;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.model.Conversation;
import com.connectly.domain.model.Message;
import com.connectly.domain.model.User;
import com.connectly.domain.port.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageServiceTest {
    private final ConversationRepository conversations = mock(ConversationRepository.class);
    private final MessageRepository messages = mock(MessageRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final BlockRepository blocks = mock(BlockRepository.class);
    private final MessageService service = new MessageService(conversations, messages, users, mock(FollowRepository.class), blocks);

    private User user(long id, AccountStatus status) {
        Instant now = Instant.now();
        return new User(id, "A", "B", "u" + id, "e" + id + "@e.com", "h", "", null, Role.USER, status, now, now);
    }

    @Test
    void cannotMessageYourself() {
        assertThrows(DomainException.class, () -> service.send(1L, 1L, "hej"));
        verify(messages, never()).save(any());
    }

    @Test
    void blockedUsersCannotMessageEachOther() {
        when(users.findById(2L)).thenReturn(Optional.of(user(2L, AccountStatus.ACTIVE)));
        when(blocks.existsEither(1L, 2L)).thenReturn(true);
        assertThrows(ForbiddenException.class, () -> service.send(1L, 2L, "hej"));
        verify(messages, never()).save(any());
    }

    @Test
    void cannotMessageInactiveUser() {
        when(users.findById(2L)).thenReturn(Optional.of(user(2L, AccountStatus.BANNED)));
        assertThrows(DomainException.class, () -> service.send(1L, 2L, "hej"));
        verify(messages, never()).save(any());
    }

    @Test
    void firstMessageCreatesConversationAndUpdatesPreview() {
        when(users.findById(2L)).thenReturn(Optional.of(user(2L, AccountStatus.ACTIVE)));
        when(conversations.findBetween(1L, 2L)).thenReturn(Optional.empty());
        when(conversations.save(any(Conversation.class))).thenAnswer(inv -> {
            Conversation c = inv.getArgument(0);
            return new Conversation(7L, c.userLowId(), c.userHighId(), c.lastMessage(), c.lastSenderId(), c.lastMessageAt());
        });
        when(messages.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            return new Message(55L, m.conversationId(), m.senderId(), m.content(), m.storyId(), m.createdAt(), null);
        });

        var view = service.send(1L, 2L, "  Zdravo!  ");

        assertEquals("Zdravo!", view.content());
        assertEquals(true, view.mine());
        verify(conversations, times(2)).save(any(Conversation.class));   // pravljenje + ažuriranje poslednje poruke
    }
}
