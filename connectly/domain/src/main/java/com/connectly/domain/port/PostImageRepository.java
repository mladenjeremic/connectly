package com.connectly.domain.port;

import com.connectly.domain.model.PostImage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostImageRepository {
    PostImage save(PostImage image);
    Optional<PostImage> findById(Long id);
    List<PostImage> findByPostIds(Collection<Long> postIds);
    int countByPostId(Long postId);
    void delete(Long id);
}
