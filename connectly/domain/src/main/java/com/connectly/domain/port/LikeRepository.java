package com.connectly.domain.port;
public interface LikeRepository {
    boolean exists(Long postId, Long userId);
    void add(Long postId, Long userId);
    void remove(Long postId, Long userId);
    long count(Long postId);
}