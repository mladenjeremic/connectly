package com.connectly.domain.model;

import com.connectly.domain.exception.DomainException;
import java.time.Instant;

/** Tekst objave je opcion (objava može imati samo slike); servis proverava da postoji tekst ili slika. */
public record Post(Long id, Long authorId, String content, Instant createdAt, Instant updatedAt) {
    public Post {
        if (content == null) content = "";
        if (content.length() > 2000)
            throw new DomainException("Tekst objave može imati najviše 2000 karaktera.");
    }

    public static Post create(Long authorId, String content) {
        Instant now = Instant.now();
        return new Post(null, authorId, content == null ? "" : content.strip(), now, now);
    }

    public Post edit(String newContent) {
        return new Post(id, authorId, newContent == null ? "" : newContent.strip(), createdAt, Instant.now());
    }
}
