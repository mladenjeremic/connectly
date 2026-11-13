package com.connectly.domain.model;

import com.connectly.domain.enums.NotificationType;
import java.time.Instant;

public record Notification(Long id, Long recipientId, Long actorId, NotificationType type,
                           Long postId, String message, boolean read, Instant createdAt) {
    public static Notification create(Long recipientId, Long actorId, NotificationType type, Long postId, String message) {
        return new Notification(null, recipientId, actorId, type, postId, message, false, Instant.now());
    }
}
