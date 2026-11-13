package com.connectly.domain.port;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<User> findByIds(Collection<Long> ids);
    /** Javna pretraga: samo ACTIVE nalozi, po username-u ili imenu i prezimenu. */
    List<User> searchPublic(String query, int page, int size);
    /** Admin pretraga: po username-u ili email adresi, opciono filtrirano po statusu. */
    List<User> searchAdmin(String query, Collection<AccountStatus> statuses, int page, int size);
    /** Predlozi za praćenje: aktivni nalozi koji nisu u excludedIds, najnoviji prvo. */
    List<User> findSuggestions(Collection<Long> excludedIds, int limit);

    long count();
    long countByStatus(AccountStatus status);
}
