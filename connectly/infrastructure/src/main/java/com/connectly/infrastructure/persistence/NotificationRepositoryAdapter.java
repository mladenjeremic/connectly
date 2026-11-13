package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Notification;
import com.connectly.domain.port.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
class NotificationRepositoryAdapter implements NotificationRepository {
    private final NotificationJpaRepository jpa;
    NotificationRepositoryAdapter(NotificationJpaRepository jpa) { this.jpa = jpa; }

    public Notification save(Notification n) { return jpa.save(NotificationEntity.from(n)).toDomain(); }
    public List<Notification> findByRecipient(Long recipientId, int page, int size) {
        return jpa.findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(page, size))
                .stream().map(NotificationEntity::toDomain).toList();
    }
    public long countUnread(Long recipientId) { return jpa.countByRecipientIdAndReadFlagFalse(recipientId); }
    public void markRead(Long id, Long recipientId) { jpa.markRead(id, recipientId); }
    public void markAllRead(Long recipientId) { jpa.markAllRead(recipientId); }
}
