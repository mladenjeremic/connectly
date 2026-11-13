package com.connectly.application;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.Role;
import com.connectly.domain.model.Ban;
import com.connectly.domain.model.User;
import com.connectly.domain.port.BanRepository;
import com.connectly.domain.port.UserRepository;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountGuardTest {
    private final UserRepository users = mock(UserRepository.class);
    private final BanRepository bans = mock(BanRepository.class);
    private final AccountGuard guard = new AccountGuard(users, bans);

    private static User suspended() {
        Instant now = Instant.now();
        return new User(1L, "F", "L", "u", "u@x.com", "h", "", null, Role.USER, AccountStatus.SUSPENDED, now, now);
    }

    @Test
    void activeBanKeepsAccountBlocked() {
        when(users.findById(1L)).thenReturn(Optional.of(suspended()));
        when(bans.findActiveByUserId(1L)).thenReturn(Optional.of(Ban.temporary(1L, 9L, "spam", Duration.ofDays(3))));
        assertTrue(guard.resolve(1L).isEmpty());
        verify(users, never()).save(any());
    }

    @Test
    void expiredBanIsLiftedAutomatically() {
        Instant past = Instant.now().minus(Duration.ofDays(2));
        Ban expired = new Ban(5L, 1L, 9L, "spam", past.minus(Duration.ofDays(1)), past, true, past);
        when(users.findById(1L)).thenReturn(Optional.of(suspended()));
        when(bans.findActiveByUserId(1L)).thenReturn(Optional.of(expired));
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        Optional<User> result = guard.resolve(1L);

        assertTrue(result.isPresent());
        assertEquals(AccountStatus.ACTIVE, result.get().status());
        verify(bans).deactivateAllForUser(1L);
    }

    @Test
    void permanentBanNeverLifts() {
        Instant now = Instant.now();
        User banned = new User(1L, "F", "L", "u", "u@x.com", "h", "", null, Role.USER, AccountStatus.BANNED, now, now);
        when(users.findById(1L)).thenReturn(Optional.of(banned));
        assertTrue(guard.resolve(1L).isEmpty());
    }
}
