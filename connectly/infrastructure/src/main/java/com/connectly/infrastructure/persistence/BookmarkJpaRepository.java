package com.connectly.infrastructure.persistence;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

interface BookmarkJpaRepository extends JpaRepository<BookmarkEntity, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);

    @Query("select b.postId from BookmarkEntity b where b.postId in :ids and b.userId = :uid")
    List<Long> savedAmong(@Param("ids") Collection<Long> ids, @Param("uid") Long userId);

    @Query("select b.postId from BookmarkEntity b where b.userId = :uid order by b.createdAt desc, b.id desc")
    List<Long> findPostIds(@Param("uid") Long userId, Pageable pageable);
}
