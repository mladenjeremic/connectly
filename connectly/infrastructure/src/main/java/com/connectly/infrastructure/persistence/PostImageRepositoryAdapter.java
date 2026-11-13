package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.PostImage;
import com.connectly.domain.port.PostImageRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
class PostImageRepositoryAdapter implements PostImageRepository {
    private final PostImageJpaRepository jpa;
    PostImageRepositoryAdapter(PostImageJpaRepository jpa) { this.jpa = jpa; }

    public PostImage save(PostImage image) { return jpa.save(PostImageEntity.from(image)).toDomain(); }
    public Optional<PostImage> findById(Long id) { return jpa.findById(id).map(PostImageEntity::toDomain); }
    public List<PostImage> findByPostIds(Collection<Long> postIds) {
        if (postIds.isEmpty()) return List.of();
        return jpa.findByPostIdIn(postIds).stream().map(PostImageEntity::toDomain).toList();
    }
    public int countByPostId(Long postId) { return jpa.countByPostId(postId); }
    public void delete(Long id) { jpa.deleteById(id); }
}
