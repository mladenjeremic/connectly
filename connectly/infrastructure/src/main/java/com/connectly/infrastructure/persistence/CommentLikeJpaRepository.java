package com.connectly.infrastructure.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

interface CommentLikeJpaRepository extends JpaRepository<CommentLikeEntity, Long> {
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
    long countByCommentId(Long commentId);

    @Query("select l.commentId, count(l) from CommentLikeEntity l where l.commentId in :ids group by l.commentId")
    List<Object[]> countByComments(@Param("ids") Collection<Long> ids);

    @Query("select l.commentId from CommentLikeEntity l where l.commentId in :ids and l.userId = :uid")
    List<Long> likedBy(@Param("ids") Collection<Long> ids, @Param("uid") Long userId);
}
