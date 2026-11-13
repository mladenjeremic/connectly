package com.connectly.domain.port;

import com.connectly.domain.model.Ban;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BanRepository {
    Ban save(Ban ban);
    Optional<Ban> findActiveByUserId(Long userId);
    List<Ban> findByUserId(Long userId);
    /** Aktivne zabrane čiji je rok istekao (za periodični job). */
    List<Ban> findExpiredActive(Instant now);
    void deactivateAllForUser(Long userId);
}
