package com.connectly.infrastructure.persistence;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "post_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"postId", "userId"}))
public class PostLikeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long postId;
    @Column(nullable = false) public Long userId;
    public Instant createdAt = Instant.now();
}