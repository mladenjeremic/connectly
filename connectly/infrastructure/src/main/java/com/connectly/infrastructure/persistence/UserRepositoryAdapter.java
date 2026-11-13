package com.connectly.infrastructure.persistence;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.model.User;
import com.connectly.domain.port.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpa;
    UserRepositoryAdapter(UserJpaRepository jpa) { this.jpa = jpa; }

    public User save(User u) { return jpa.save(UserEntity.from(u)).toDomain(); }
    public Optional<User> findById(Long id) { return jpa.findById(id).map(UserEntity::toDomain); }
    public Optional<User> findByEmail(String e) { return jpa.findByEmail(e).map(UserEntity::toDomain); }
    public Optional<User> findByUsername(String username) { return jpa.findByUsername(username).map(UserEntity::toDomain); }
    public boolean existsByEmail(String e) { return jpa.existsByEmail(e); }
    public boolean existsByUsername(String u) { return jpa.existsByUsername(u); }

    public List<User> findByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return jpa.findByIdIn(ids).stream().map(UserEntity::toDomain).toList();
    }

    public List<User> searchPublic(String query, int page, int size) {
        return jpa.searchPublic(pattern(query), AccountStatus.ACTIVE, PageRequest.of(page, size))
                .stream().map(UserEntity::toDomain).toList();
    }

    public List<User> searchAdmin(String query, Collection<AccountStatus> statuses, int page, int size) {
        if (statuses.isEmpty()) return List.of();
        return jpa.searchAdmin(pattern(query), statuses, PageRequest.of(page, size))
                .stream().map(UserEntity::toDomain).toList();
    }

    public List<User> findSuggestions(Collection<Long> excludedIds, int limit) {
        Collection<Long> excluded = excludedIds.isEmpty() ? List.of(-1L) : excludedIds;
        return jpa.findSuggestions(excluded, AccountStatus.ACTIVE, PageRequest.of(0, limit))
                .stream().map(UserEntity::toDomain).toList();
    }

    public long count() { return jpa.count(); }
    public long countByStatus(AccountStatus status) { return jpa.countByStatus(status); }

    private static String pattern(String query) {
        String q = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        if (q.startsWith("@")) q = q.substring(1);
        return "%" + q + "%";
    }
}
