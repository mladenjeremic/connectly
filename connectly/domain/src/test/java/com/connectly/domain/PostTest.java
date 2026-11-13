package com.connectly.domain;

import com.connectly.domain.exception.DomainException;
import com.connectly.domain.model.Post;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PostTest {
    @Test void trimsContent() { assertEquals("hi", Post.create(1L, " hi ").content()); }
    @Test void allowsEmptyCaptionBecausePostCanHaveOnlyImages() { assertEquals("", Post.create(1L, "  ").content()); }
    @Test void rejectsTooLongContent() {
        assertThrows(DomainException.class, () -> Post.create(1L, "x".repeat(2001)));
    }
    @Test void editKeepsAuthorAndCreationTime() {
        Post original = Post.create(7L, "old");
        Post edited = original.edit(" new ");
        assertEquals("new", edited.content());
        assertEquals(7L, edited.authorId());
        assertEquals(original.createdAt(), edited.createdAt());
    }
}
