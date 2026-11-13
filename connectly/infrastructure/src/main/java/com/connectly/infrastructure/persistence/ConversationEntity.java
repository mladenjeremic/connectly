package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.Conversation;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conversations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userLowId", "userHighId"}),
        indexes = { @Index(columnList = "userLowId"), @Index(columnList = "userHighId") })
public class ConversationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long userLowId;
    @Column(nullable = false) public Long userHighId;
    @Column(length = 500) public String lastMessage;
    public Long lastSenderId;
    public Instant lastMessageAt;

    static ConversationEntity from(Conversation c) {
        ConversationEntity e = new ConversationEntity();
        e.id = c.id(); e.userLowId = c.userLowId(); e.userHighId = c.userHighId();
        e.lastMessage = c.lastMessage(); e.lastSenderId = c.lastSenderId(); e.lastMessageAt = c.lastMessageAt();
        return e;
    }

    Conversation toDomain() { return new Conversation(id, userLowId, userHighId, lastMessage, lastSenderId, lastMessageAt); }
}
