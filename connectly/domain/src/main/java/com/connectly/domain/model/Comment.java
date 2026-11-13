package com.connectly.domain.model;

import com.connectly.domain.exception.DomainException;
import java.time.Instant;

/** Komentar na objavi. {@code parentId} je null za komentar prvog nivoa, a za odgovor id komentara na koji se odgovara (uvek koren niti). */
public record Comment(Long id, Long postId, Long parentId, Long authorId, String content, Instant createdAt) {
    public Comment {
        if (content == null || content.isBlank() || content.length() > 1000)
            throw new DomainException("Komentar mora imati 1-1000 karaktera.");
    }

    public Comment(Long id, Long postId, Long authorId, String content, Instant createdAt) {
        this(id, postId, null, authorId, content, createdAt);
    }

    public static Comment create(Long postId, Long authorId, String content) {
        return reply(postId, null, authorId, content);
    }

    public static Comment reply(Long postId, Long parentId, Long authorId, String content) {
        if (content == null) throw new DomainException("Komentar mora imati 1-1000 karaktera.");
        return new Comment(null, postId, parentId, authorId, content.strip(), Instant.now());
    }
}
