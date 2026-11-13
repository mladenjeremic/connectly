package com.connectly.infrastructure.persistence;

import com.connectly.domain.enums.AccountStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<UserEntity> findByUsername(String username);
    List<UserEntity> findByIdIn(Collection<Long> ids);
    long countByStatus(AccountStatus status);

    @Query("select u from UserEntity u where u.status = :active "
            + "and (lower(u.username) like :q or lower(concat(coalesce(u.firstName, ''), ' ', coalesce(u.lastName, ''))) like :q) "
            + "order by u.username")
    List<UserEntity> searchPublic(@Param("q") String q, @Param("active") AccountStatus active, Pageable pageable);

    @Query("select u from UserEntity u where u.status in :statuses "
            + "and (lower(u.username) like :q or lower(u.email) like :q) order by u.createdAt desc")
    List<UserEntity> searchAdmin(@Param("q") String q, @Param("statuses") Collection<AccountStatus> statuses, Pageable pageable);

    @Query("select u from UserEntity u where u.status = :active and u.id not in :excluded order by u.createdAt desc")
    List<UserEntity> findSuggestions(@Param("excluded") Collection<Long> excluded, @Param("active") AccountStatus active, Pageable pageable);
}
