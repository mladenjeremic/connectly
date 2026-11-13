package com.connectly.infrastructure.persistence;
import com.connectly.domain.port.LikeRepository;
import org.springframework.stereotype.Repository;

@Repository
class LikeRepositoryAdapter implements LikeRepository {
    private final LikeJpaRepository jpa;
    LikeRepositoryAdapter(LikeJpaRepository jpa) { this.jpa = jpa; }

    public boolean exists(Long postId, Long userId) { return jpa.existsByPostIdAndUserId(postId, userId); }
    public void add(Long postId, Long userId) {
        PostLikeEntity e = new PostLikeEntity();
        e.postId = postId; e.userId = userId;
        jpa.save(e);
    }
    public void remove(Long postId, Long userId) { jpa.deleteByPostIdAndUserId(postId, userId); }
    public long count(Long postId) { return jpa.countByPostId(postId); }
}