package com.connectly.infrastructure.persistence;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_blocks", uniqueConstraints = @UniqueConstraint(columnNames = {"blockerId", "blockedId"}),
        indexes = {@Index(columnList = "blockerId"), @Index(columnList = "blockedId")})
public class BlockEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long blockerId;
    @Column(nullable = false) public Long blockedId;
    public Instant createdAt = Instant.now();
}
