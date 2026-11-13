package com.connectly.domain.port;

import java.util.List;

public interface BlockRepository {
    boolean exists(Long blockerId, Long blockedId);
    /** Da li je bilo ko od njih blokirao drugog. */
    boolean existsEither(Long userA, Long userB);
    void add(Long blockerId, Long blockedId);
    void remove(Long blockerId, Long blockedId);
    /** Koga je ovaj korisnik blokirao, poslednje blokirani prvi. */
    List<Long> findBlockedIds(Long blockerId);
    /** Ko je blokirao ovog korisnika. */
    List<Long> findBlockerIds(Long blockedId);
}
