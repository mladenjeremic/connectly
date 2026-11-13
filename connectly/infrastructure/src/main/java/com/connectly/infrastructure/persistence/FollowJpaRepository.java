package com.connectly.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

interface FollowJpaRepository extends JpaRepository<FollowEntity, Long> {
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
    long countByFollowedId(Long followedId);
    long countByFollowerId(Long followerId);

    @Query("select f.followedId from FollowEntity f where f.followerId = :followerId")
    List<Long> findFollowedIds(@Param("followerId") Long followerId);

    @Query("select f.followerId from FollowEntity f where f.followedId = :userId order by f.createdAt desc")
    List<Long> findFollowerIds(@Param("userId") Long userId, Pageable pageable);

    @Query("select f.followedId from FollowEntity f where f.followerId = :userId order by f.createdAt desc")
    List<Long> findFollowingIds(@Param("userId") Long userId, Pageable pageable);
}
