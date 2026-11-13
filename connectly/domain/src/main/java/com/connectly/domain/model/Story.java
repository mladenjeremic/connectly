package com.connectly.domain.model;

import java.time.Duration;
import java.time.Instant;

public record Story(Long id, Long authorId, String fileName, Instant createdAt, Instant expiresAt) {
    public static final Duration LIFETIME = Duration.ofHours(24);

    public static Story create(Long authorId, String fileName) {
        Instant now = Instant.now();
        return new Story(null, authorId, fileName, now, now.plus(LIFETIME));
    }
}
