package com.connectly.domain.port;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface CommentLikeRepository {
    boolean exists(Long commentId, Long userId);
    void add(Long commentId, Long userId);
    void remove(Long commentId, Long userId);
    long count(Long commentId);
    /** Broj lajkova po komentaru (komentari bez lajkova nisu u mapi). */
    Map<Long, Long> countByComments(Collection<Long> commentIds);
    /** Id-jevi komentara iz liste koje je ovaj korisnik lajkovao. */
    Set<Long> likedBy(Collection<Long> commentIds, Long userId);
}
