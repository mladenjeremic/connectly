package com.connectly.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface BookmarkRepository {
    boolean exists(Long postId, Long userId);
    void add(Long postId, Long userId);
    void remove(Long postId, Long userId);
    /** Id-jevi objava iz liste koje je korisnik sačuvao. */
    Set<Long> savedAmong(Collection<Long> postIds, Long userId);
    /** Id-jevi sačuvanih objava, poslednje sačuvana prva. */
    List<Long> findPostIds(Long userId, int page, int size);
}
