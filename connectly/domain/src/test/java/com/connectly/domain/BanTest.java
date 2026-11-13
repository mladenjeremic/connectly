package com.connectly.domain;

import com.connectly.domain.model.Ban;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class BanTest {
    @Test void temporaryBanExpiresAfterItsEnd() {
        Ban ban = Ban.temporary(1L, 2L, "spam", Duration.ofDays(1));
        assertFalse(ban.isExpiredAt(Instant.now()));
        assertTrue(ban.isExpiredAt(Instant.now().plus(Duration.ofDays(2))));
    }

    @Test void permanentBanNeverExpires() {
        Ban ban = Ban.permanent(1L, 2L, "abuse");
        assertTrue(ban.isPermanent());
        assertFalse(ban.isExpiredAt(Instant.now().plus(Duration.ofDays(36500))));
    }
}
