package com.connectly.application;

import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.model.Post;
import com.connectly.domain.port.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostServiceTest {
    private final PostRepository posts = mock(PostRepository.class);
    private final PostService service = new PostService(posts, mock(PostImageRepository.class), mock(LikeRepository.class),
            mock(CommentRepository.class), mock(UserRepository.class), mock(FollowRepository.class),
            mock(ImageStorage.class), mock(EventPublisher.class), mock(BookmarkRepository.class));

    @Test
    void postNeedsTextOrImage() {
        assertThrows(DomainException.class, () -> service.create(1L, "   ", List.of()));
        verify(posts, never()).save(any());
    }

    @Test
    void strangerCannotEditPost() {
        Instant now = Instant.now();
        when(posts.findById(5L)).thenReturn(Optional.of(new Post(5L, 3L, "p", now, now)));
        assertThrows(ForbiddenException.class, () -> service.edit(5L, 9L, "hacked"));
        verify(posts, never()).save(any());
    }

    @Test
    void strangerCannotDeletePost() {
        Instant now = Instant.now();
        when(posts.findById(5L)).thenReturn(Optional.of(new Post(5L, 3L, "p", now, now)));
        assertThrows(ForbiddenException.class, () -> service.delete(5L, 9L));
        verify(posts, never()).softDelete(any());
    }
}
