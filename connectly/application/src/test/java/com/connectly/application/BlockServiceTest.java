package com.connectly.application;

import com.connectly.domain.exception.DomainException;
import com.connectly.domain.port.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BlockServiceTest {
    private final BlockRepository blocks = mock(BlockRepository.class);
    private final FollowRepository follows = mock(FollowRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final BlockService service = new BlockService(blocks, follows, users);

    @Test
    void cannotBlockYourself() {
        assertThrows(DomainException.class, () -> service.block(1L, 1L));
        verify(blocks, never()).add(any(), any());
    }
}
