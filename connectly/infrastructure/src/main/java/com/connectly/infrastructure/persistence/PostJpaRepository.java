package com.connectly.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

interface PostJpaRepository extends JpaRepository<PostEntity, Long> {
    @Query("select p from PostEntity p where p.deletedAt is null order by p.createdAt desc")
    List<PostEntity> findLatest(Pageable pageable);

    @Query("select p from PostEntity p where p.deletedAt is null and p.authorId in :ids order by p.createdAt desc")
    List<PostEntity> findByAuthors(@Param("ids") Collection<Long> ids, Pageable pageable);

    long countByDeletedAtIsNull();
    long countByAuthorIdAndDeletedAtIsNull(Long authorId);
}
