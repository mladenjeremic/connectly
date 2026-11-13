package com.connectly.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

interface PostImageJpaRepository extends JpaRepository<PostImageEntity, Long> {
    List<PostImageEntity> findByPostIdIn(Collection<Long> postIds);
    int countByPostId(Long postId);
}
