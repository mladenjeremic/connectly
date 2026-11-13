package com.connectly.infrastructure.persistence;
import org.springframework.data.jpa.repository.JpaRepository;

interface LikeJpaRepository extends JpaRepository<PostLikeEntity, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
}