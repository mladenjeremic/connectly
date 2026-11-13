package com.connectly.infrastructure.persistence;
import com.connectly.domain.port.BlockRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
class BlockRepositoryAdapter implements BlockRepository {
    private final BlockJpaRepository jpa;
    BlockRepositoryAdapter(BlockJpaRepository jpa) { this.jpa = jpa; }

    public boolean exists(Long blockerId, Long blockedId) { return jpa.existsByBlockerIdAndBlockedId(blockerId, blockedId); }
    public boolean existsEither(Long a, Long b) { return jpa.existsEither(a, b); }
    public void add(Long blockerId, Long blockedId) {
        BlockEntity e = new BlockEntity();
        e.blockerId = blockerId; e.blockedId = blockedId;
        jpa.save(e);
    }
    public void remove(Long blockerId, Long blockedId) { jpa.deleteByBlockerIdAndBlockedId(blockerId, blockedId); }
    public List<Long> findBlockedIds(Long blockerId) { return jpa.findBlockedIds(blockerId); }
    public List<Long> findBlockerIds(Long blockedId) { return jpa.findBlockerIds(blockedId); }
}
