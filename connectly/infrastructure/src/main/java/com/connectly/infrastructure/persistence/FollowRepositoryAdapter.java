package com.connectly.infrastructure.persistence;

import com.connectly.domain.port.FollowRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
class FollowRepositoryAdapter implements FollowRepository {
    private final FollowJpaRepository jpa;
    FollowRepositoryAdapter(FollowJpaRepository jpa) { this.jpa = jpa; }

    public boolean exists(Long followerId, Long followedId) { return jpa.existsByFollowerIdAndFollowedId(followerId, followedId); }
    public void add(Long followerId, Long followedId) {
        FollowEntity e = new FollowEntity();
        e.followerId = followerId; e.followedId = followedId;
        jpa.save(e);
    }
    public void remove(Long followerId, Long followedId) { jpa.deleteByFollowerIdAndFollowedId(followerId, followedId); }
    public long countFollowers(Long userId) { return jpa.countByFollowedId(userId); }
    public long countFollowing(Long userId) { return jpa.countByFollowerId(userId); }
    public List<Long> findFollowedIds(Long followerId) { return jpa.findFollowedIds(followerId); }
    public List<Long> findFollowerIds(Long userId, int page, int size) { return jpa.findFollowerIds(userId, PageRequest.of(page, size)); }
    public List<Long> findFollowingIds(Long userId, int page, int size) { return jpa.findFollowingIds(userId, PageRequest.of(page, size)); }
}
