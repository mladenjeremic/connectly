package com.connectly.infrastructure.persistence;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPostIdAndParentIdIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId, Pageable pageable);
    List<CommentEntity> findByParentIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long parentId, Pageable pageable);
    long countByPostIdAndDeletedAtIsNull(Long postId);

    @Query("select c.parentId, count(c) from CommentEntity c where c.parentId in :ids and c.deletedAt is null group by c.parentId")
    List<Object[]> countReplies(@Param("ids") Collection<Long> ids);

    @Modifying
    @Query("update CommentEntity c set c.deletedAt = :now where c.parentId = :parentId and c.deletedAt is null")
    void softDeleteReplies(@Param("parentId") Long parentId, @Param("now") Instant now);
}
