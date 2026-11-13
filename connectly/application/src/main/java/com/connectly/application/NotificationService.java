package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.enums.NotificationType;
import com.connectly.domain.model.Notification;
import com.connectly.domain.model.User;
import com.connectly.domain.port.NotificationRepository;
import com.connectly.domain.port.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
public class NotificationService implements NotificationOps {
    private final NotificationRepository notifications;
    private final UserRepository users;

    public NotificationService(NotificationRepository notifications, UserRepository users) {
        this.notifications = notifications; this.users = users;
    }

    /** Poziva se iz event listenera. Ne obaveštavamo korisnika o njegovim sopstvenim akcijama. */
    public void record(Long recipientId, Long actorId, NotificationType type, Long postId, String message) {
        if (recipientId == null || recipientId.equals(actorId)) return;
        notifications.save(Notification.create(recipientId, actorId, type, postId, message));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationView> list(Long userId, int page, int size) {
        List<Notification> list = notifications.findByRecipient(userId, Math.max(0, page), Math.max(1, Math.min(size, 50)));
        Map<Long, User> actors = new HashMap<>();
        for (Notification n : list) {
            if (n.actorId() != null && !actors.containsKey(n.actorId()))
                users.findById(n.actorId()).ifPresent(u -> actors.put(u.id(), u));
        }
        return list.stream().map(n -> {
            User a = n.actorId() == null ? null : actors.get(n.actorId());
            return new NotificationView(n.id(), n.type(), n.actorId(),
                    a == null ? null : a.username(), a == null ? null : ImageRules.url(a.profileImage()),
                    n.postId(), n.message(), n.read(), n.createdAt());
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) { return notifications.countUnread(userId); }

    @Override
    public void markRead(Long notificationId, Long userId) { notifications.markRead(notificationId, userId); }

    @Override
    public void markAllRead(Long userId) { notifications.markAllRead(userId); }
}
