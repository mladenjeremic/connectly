package com.connectly.infrastructure.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

interface BlockJpaRepository extends JpaRepository<BlockEntity, Long> {
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("select count(b) > 0 from BlockEntity b where (b.blockerId = :a and b.blockedId = :b) or (b.blockerId = :b and b.blockedId = :a)")
    boolean existsEither(@Param("a") Long a, @Param("b") Long b);

    @Query("select b.blockedId from BlockEntity b where b.blockerId = :uid order by b.createdAt desc, b.id desc")
    List<Long> findBlockedIds(@Param("uid") Long blockerId);

    @Query("select b.blockerId from BlockEntity b where b.blockedId = :uid")
    List<Long> findBlockerIds(@Param("uid") Long blockedId);
}
