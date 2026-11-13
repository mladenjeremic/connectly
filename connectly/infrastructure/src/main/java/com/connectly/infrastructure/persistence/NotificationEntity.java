package com.connectly.infrastructure.persistence;

import com.connectly.domain.enums.NotificationType;
import com.connectly.domain.model.Notification;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity @Table(name = "notifications", indexes = @Index(columnList = "recipientId"))
public class NotificationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long recipientId;
    public Long actorId;
    // VARCHAR umesto MySQL ENUM kolone, da novi tipovi obaveštenja ne traže izmenu šeme
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) public NotificationType type;
    public Long postId;
    @Column(length = 600) public String message;
    // "read" je rezervisana reč u nekim bazama, zato eksplicitno ime kolone
    @Column(name = "is_read", nullable = false) public boolean readFlag;
    public Instant createdAt;

    static NotificationEntity from(Notification n) {
        NotificationEntity e = new NotificationEntity();
        e.id = n.id(); e.recipientId = n.recipientId(); e.actorId = n.actorId(); e.type = n.type();
        e.postId = n.postId(); e.message = n.message(); e.readFlag = n.read(); e.createdAt = n.createdAt();
        return e;
    }

    Notification toDomain() { return new Notification(id, recipientId, actorId, type, postId, message, readFlag, createdAt); }
}
