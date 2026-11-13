package com.connectly.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    long countByRecipientIdAndReadFlagFalse(Long recipientId);

    @Modifying
    @Query("update NotificationEntity n set n.readFlag = true where n.id = :id and n.recipientId = :uid")
    void markRead(@Param("id") Long id, @Param("uid") Long recipientId);

    @Modifying
    @Query("update NotificationEntity n set n.readFlag = true where n.recipientId = :uid and n.readFlag = false")
    void markAllRead(@Param("uid") Long recipientId);
}
