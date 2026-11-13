package com.connectly.domain.port;

import com.connectly.domain.model.Notification;
import java.util.List;

public interface NotificationRepository {
    Notification save(Notification notification);
    List<Notification> findByRecipient(Long recipientId, int page, int size);
    long countUnread(Long recipientId);
    void markRead(Long id, Long recipientId);
    void markAllRead(Long recipientId);
}
