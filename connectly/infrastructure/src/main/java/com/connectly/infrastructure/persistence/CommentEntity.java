package com.connectly.infrastructure.persistence;
import com.connectly.domain.model.Comment;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "comments", indexes = { @Index(columnList = "postId"), @Index(columnList = "parentId") })
public class CommentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long postId;
    /** null = komentar prvog nivoa; inače id korenskog komentara niti */
    public Long parentId;
    @Column(nullable = false) public Long authorId;
    @Column(nullable = false, length = 1000) public String content;
    public Instant createdAt;
    public Instant deletedAt;

    static CommentEntity from(Comment c) {
        CommentEntity e = new CommentEntity();
        e.id = c.id(); e.postId = c.postId(); e.parentId = c.parentId(); e.authorId = c.authorId();
        e.content = c.content(); e.createdAt = c.createdAt();
        return e;
    }
    Comment toDomain() { return new Comment(id, postId, parentId, authorId, content, createdAt); }
}
