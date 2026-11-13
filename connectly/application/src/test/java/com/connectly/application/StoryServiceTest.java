package com.connectly.application;

import com.connectly.domain.event.StoryReplied;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.model.Story;
import com.connectly.domain.port.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoryServiceTest {
    private final StoryRepository stories = mock(StoryRepository.class);
    private final FollowRepository follows = mock(FollowRepository.class);
    private final EventPublisher events = mock(EventPublisher.class);
    private final BlockRepository blocks = mock(BlockRepository.class);
    private final StoryService service = new StoryService(stories, mock(UserRepository.class), follows,
            mock(ImageStorage.class), events, blocks);

    private void storyOf(long authorId) {
        Instant now = Instant.now();
        when(stories.findById(1L)).thenReturn(Optional.of(new Story(1L, authorId, "f.jpg", now, now.plusSeconds(3600))));
    }

    @Test
    void followerCanReplyToStory() {
        storyOf(2L);
        when(follows.exists(9L, 2L)).thenReturn(true);
        service.reply(1L, 9L, "  Super slika!  ");
        verify(events).publish(any(StoryReplied.class));
    }

    @Test
    void strangerCannotReplyToStory() {
        storyOf(2L);
        when(follows.exists(9L, 2L)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> service.reply(1L, 9L, "hej"));
        verify(events, never()).publish(any());
    }

    @Test
    void blockedUserCannotReplyToStory() {
        storyOf(2L);
        when(blocks.existsEither(9L, 2L)).thenReturn(true);
        assertThrows(ForbiddenException.class, () -> service.reply(1L, 9L, "hej"));
        verify(events, never()).publish(any());
    }

    @Test
    void cannotReplyToOwnStory() {
        storyOf(2L);
        assertThrows(DomainException.class, () -> service.reply(1L, 2L, "hej"));
        verify(events, never()).publish(any());
    }

    @Test
    void emptyReplyIsRejected() {
        storyOf(2L);
        when(follows.exists(9L, 2L)).thenReturn(true);
        assertThrows(DomainException.class, () -> service.reply(1L, 9L, "   "));
        verify(events, never()).publish(any());
    }
}
