package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Ban;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "bans", indexes = @Index(columnList = "userId"))
public class BanEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long userId;
    @Column(nullable = false) public Long adminId;
    @Column(nullable = false, length = 500) public String reason;
    public Instant startsAt;
    public Instant endsAt;          // null = trajna zabrana
    @Column(nullable = false) public boolean active;
    public Instant createdAt;

    static BanEntity from(Ban b) {
        BanEntity e = new BanEntity();
        e.id = b.id(); e.userId = b.userId(); e.adminId = b.adminId(); e.reason = b.reason();
        e.startsAt = b.startsAt(); e.endsAt = b.endsAt(); e.active = b.active(); e.createdAt = b.createdAt();
        return e;
    }

    Ban toDomain() { return new Ban(id, userId, adminId, reason, startsAt, endsAt, active, createdAt); }
}
