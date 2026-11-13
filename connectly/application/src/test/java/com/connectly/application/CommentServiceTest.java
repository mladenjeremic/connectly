package com.connectly.application;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.Role;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.model.*;
import com.connectly.domain.port.*;
import org.junit.jupiter.api.Test;
import com.connectly.domain.event.CommentLiked;
import com.connectly.domain.event.CommentReplied;
import org.mockito.ArgumentCaptor;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CommentServiceTest {
    @Test
    void strangerCannotDeleteComment() {
        var comments = mock(CommentRepository.class);
        var posts = mock(PostRepository.class);
        var users = mock(UserRepository.class);
        Instant now = Instant.now();
        when(comments.findById(10L)).thenReturn(Optional.of(new Comment(10L, 5L, 2L, "x", now)));
        when(posts.findById(5L)).thenReturn(Optional.of(new Post(5L, 3L, "p", now, now)));
        when(users.findById(9L)).thenReturn(Optional.of(
                new User(9L, "A", "B", "u", "e@e.com", "h", "", null, Role.USER, AccountStatus.ACTIVE, now, now)));

        var service = new CommentService(comments, mock(CommentLikeRepository.class), posts, users, mock(EventPublisher.class));

        assertThrows(ForbiddenException.class, () -> service.delete(10L, 9L));
        verify(comments, never()).softDelete(anyLong());
    }

    @Test
    void replyToReplyLandsInRootThread() {
        var comments = mock(CommentRepository.class);
        var posts = mock(PostRepository.class);
        var users = mock(UserRepository.class);
        var events = mock(EventPublisher.class);
        Instant now = Instant.now();
        when(posts.findById(5L)).thenReturn(Optional.of(new Post(5L, 3L, "p", now, now)));
        // komentar 11 je odgovor u niti čiji je koren komentar 10
        when(comments.findById(11L)).thenReturn(Optional.of(new Comment(11L, 5L, 10L, 2L, "odgovor", now)));
        when(comments.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            return new Comment(99L, c.postId(), c.parentId(), c.authorId(), c.content(), c.createdAt());
        });
        when(users.findById(any())).thenReturn(Optional.empty());
        when(users.findByIds(any())).thenReturn(List.of());

        var service = new CommentService(comments, mock(CommentLikeRepository.class), posts, users, events);
        service.add(5L, 7L, 11L, "  hej  ");

        var saved = ArgumentCaptor.forClass(Comment.class);
        verify(comments).save(saved.capture());
        assertEquals(10L, saved.getValue().parentId());
        assertEquals("hej", saved.getValue().content());
        verify(events).publish(any(CommentReplied.class));
    }

    @Test
    void likingCommentTwiceNotifiesOnce() {
        var comments = mock(CommentRepository.class);
        var likes = mock(CommentLikeRepository.class);
        var posts = mock(PostRepository.class);
        var events = mock(EventPublisher.class);
        Instant now = Instant.now();
        when(comments.findById(10L)).thenReturn(Optional.of(new Comment(10L, 5L, 2L, "x", now)));
        when(posts.findById(5L)).thenReturn(Optional.of(new Post(5L, 3L, "p", now, now)));
        when(likes.exists(10L, 9L)).thenReturn(false, true);
        when(likes.count(10L)).thenReturn(1L);

        var service = new CommentService(comments, likes, posts, mock(UserRepository.class), events);
        service.like(10L, 9L);
        service.like(10L, 9L);

        verify(likes, times(1)).add(10L, 9L);
        verify(events, times(1)).publish(any(CommentLiked.class));
    }
}
