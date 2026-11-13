package com.connectly.infrastructure.persistence;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bookmarks", uniqueConstraints = @UniqueConstraint(columnNames = {"postId", "userId"}),
        indexes = @Index(columnList = "userId"))
public class BookmarkEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long postId;
    @Column(nullable = false) public Long userId;
    public Instant createdAt = Instant.now();
}
