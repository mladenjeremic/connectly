package com.connectly.domain.port;

import java.util.List;

public interface FollowRepository {
    boolean exists(Long followerId, Long followedId);
    void add(Long followerId, Long followedId);
    void remove(Long followerId, Long followedId);
    long countFollowers(Long userId);
    long countFollowing(Long userId);
    List<Long> findFollowedIds(Long followerId);
    List<Long> findFollowerIds(Long userId, int page, int size);
    List<Long> findFollowingIds(Long userId, int page, int size);
}
