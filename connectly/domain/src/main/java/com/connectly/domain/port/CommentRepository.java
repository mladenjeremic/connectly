package com.connectly.domain.port;

import com.connectly.domain.model.Comment;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment);
    Optional<Comment> findById(Long id);
    /** Samo komentari prvog nivoa (bez odgovora), stariji prvo. */
    List<Comment> findByPostId(Long postId, int page, int size);
    /** Odgovori na jedan komentar, stariji prvo. */
    List<Comment> findReplies(Long parentId, int page, int size);
    /** Broj neobrisanih odgovora po komentaru (komentari bez odgovora nisu u mapi). */
    Map<Long, Long> countRepliesByParents(Collection<Long> parentIds);
    /** Broj svih neobrisanih komentara i odgovora na objavi. */
    long count(Long postId);
    /** Briše komentar, a ako je koren niti i sve njegove odgovore. */
    void softDelete(Long id);
}
