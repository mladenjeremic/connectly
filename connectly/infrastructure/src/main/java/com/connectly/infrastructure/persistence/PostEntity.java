package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Post;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "posts", indexes = @Index(columnList = "createdAt"))
public class PostEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long authorId;
    @Column(nullable = false, length = 2000) public String content;
    public Instant createdAt;
    public Instant updatedAt;
    public Instant deletedAt;

    Post toDomain() { return new Post(id, authorId, content, createdAt, updatedAt == null ? createdAt : updatedAt); }
}
