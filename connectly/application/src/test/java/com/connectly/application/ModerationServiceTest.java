package com.connectly.application;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.Role;
import com.connectly.domain.event.AccountStatusChanged;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.model.Ban;
import com.connectly.domain.model.User;
import com.connectly.domain.port.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModerationServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PostRepository posts = mock(PostRepository.class);
    private final BanRepository bans = mock(BanRepository.class);
    private final AdminActionRepository actions = mock(AdminActionRepository.class);
    private final EventPublisher events = mock(EventPublisher.class);
    private final ModerationService service = new ModerationService(users, posts, mock(PostImageRepository.class),
            mock(CommentRepository.class), mock(ReportRepository.class), bans, actions, events);

    private static User user(long id, Role role, AccountStatus status) {
        Instant now = Instant.now();
        return new User(id, "F", "L", "user" + id, "u" + id + "@x.com", "h", "", null, role, status, now, now);
    }

    @Test
    void banSuspendsAccountLogsActionAndNotifiesUser() {
        when(users.findById(2L)).thenReturn(Optional.of(user(2L, Role.USER, AccountStatus.ACTIVE)));
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(bans.save(any(Ban.class))).thenAnswer(i -> i.getArgument(0));

        service.ban(1L, 2L, 7, "spam");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals(AccountStatus.SUSPENDED, saved.getValue().status());
        verify(actions).save(any());
        verify(events).publish(any(AccountStatusChanged.class));
    }

    @Test
    void reasonIsRequired() {
        assertThrows(DomainException.class, () -> service.ban(1L, 2L, 7, "  "));
        verify(users, never()).save(any());
    }

    @Test
    void adminCannotBanThemselvesOrAnotherAdmin() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L, Role.ADMIN, AccountStatus.ACTIVE)));
        when(users.findById(3L)).thenReturn(Optional.of(user(3L, Role.ADMIN, AccountStatus.ACTIVE)));
        assertThrows(ForbiddenException.class, () -> service.permanentBan(1L, 1L, "x"));
        assertThrows(ForbiddenException.class, () -> service.permanentBan(1L, 3L, "x"));
        verify(users, never()).save(any());
    }

    @Test
    void banLengthIsLimited() {
        assertThrows(DomainException.class, () -> service.ban(1L, 2L, 0, "r"));
        assertThrows(DomainException.class, () -> service.ban(1L, 2L, 366, "r"));
    }
}
