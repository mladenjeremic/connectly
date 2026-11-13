package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Message;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "messages", indexes = @Index(columnList = "conversationId, createdAt"))
public class MessageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long conversationId;
    @Column(nullable = false) public Long senderId;
    @Column(nullable = false, length = 2000) public String content;
    /** popunjeno kad je poruka odgovor na priču */
    public Long storyId;
    public Instant createdAt;
    public Instant readAt;

    static MessageEntity from(Message m) {
        MessageEntity e = new MessageEntity();
        e.id = m.id(); e.conversationId = m.conversationId(); e.senderId = m.senderId(); e.content = m.content();
        e.storyId = m.storyId(); e.createdAt = m.createdAt(); e.readAt = m.readAt();
        return e;
    }

    Message toDomain() { return new Message(id, conversationId, senderId, content, storyId, createdAt, readAt); }
}
