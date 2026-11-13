package com.connectly.domain.model;

import java.time.Duration;
import java.time.Instant;

/** endsAt == null znači trajnu zabranu. */
public record Ban(Long id, Long userId, Long adminId, String reason, Instant startsAt, Instant endsAt,
                  boolean active, Instant createdAt) {

    public static Ban temporary(Long userId, Long adminId, String reason, Duration length) {
        Instant now = Instant.now();
        return new Ban(null, userId, adminId, reason, now, now.plus(length), true, now);
    }

    public static Ban permanent(Long userId, Long adminId, String reason) {
        Instant now = Instant.now();
        return new Ban(null, userId, adminId, reason, now, null, true, now);
    }

    public boolean isPermanent() { return endsAt == null; }

    public boolean isExpiredAt(Instant moment) { return endsAt != null && !endsAt.isAfter(moment); }
}
