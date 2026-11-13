package com.connectly.infrastructure.persistence;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comment_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"commentId", "userId"}))
public class CommentLikeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long commentId;
    @Column(nullable = false) public Long userId;
    public Instant createdAt = Instant.now();
}
