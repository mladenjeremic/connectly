package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Story;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "stories", indexes = @Index(columnList = "authorId"))
public class StoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long authorId;
    @Column(nullable = false, length = 100) public String fileName;
    public Instant createdAt;
    public Instant expiresAt;

    static StoryEntity from(Story s) {
        StoryEntity e = new StoryEntity();
        e.id = s.id(); e.authorId = s.authorId(); e.fileName = s.fileName();
        e.createdAt = s.createdAt(); e.expiresAt = s.expiresAt();
        return e;
    }

    Story toDomain() { return new Story(id, authorId, fileName, createdAt, expiresAt); }
}
