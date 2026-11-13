package com.connectly.application;

import com.connectly.domain.exception.DomainException;
import com.connectly.domain.port.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SocialServiceTest {
    @Test
    void cannotFollowYourself() {
        var service = new SocialService(mock(UserRepository.class), mock(FollowRepository.class),
                mock(PostRepository.class), mock(EventPublisher.class), mock(BlockRepository.class));
        assertThrows(DomainException.class, () -> service.follow(1L, 1L));
    }
}
