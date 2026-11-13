package com.connectly.infrastructure.persistence;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = {"followerId", "followedId"}))
public class FollowEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long followerId;
    @Column(nullable = false) public Long followedId;
    public Instant createdAt = Instant.now();
}