package com.connectly.domain.port;

import com.connectly.domain.model.Post;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(Long id);
    List<Post> findLatest(int page, int size);
    List<Post> findByAuthors(Collection<Long> authorIds, int page, int size);
    void softDelete(Long id);
    long count();
    long countByAuthor(Long authorId);
}
