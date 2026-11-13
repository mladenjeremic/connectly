package com.connectly.domain.model;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.Role;
import java.time.Instant;

public record User(Long id, String firstName, String lastName, String username, String email,
                   String passwordHash, String bio, String profileImage,
                   Role role, AccountStatus status, Instant createdAt, Instant updatedAt) {

    public static User register(String firstName, String lastName, String username, String email, String passwordHash) {
        Instant now = Instant.now();
        return new User(null, firstName, lastName, username, email, passwordHash, "", null,
                Role.USER, AccountStatus.ACTIVE, now, now);
    }

    public boolean isActive() { return status == AccountStatus.ACTIVE; }
    public boolean isAdmin() { return role == Role.ADMIN; }

    public String fullName() {
        String full = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        return full.isEmpty() ? username : full;
    }

    public User withProfile(String firstName, String lastName, String bio) {
        return new User(id, firstName, lastName, username, email, passwordHash, bio, profileImage,
                role, status, createdAt, Instant.now());
    }

    public User withProfileImage(String fileName) {
        return new User(id, firstName, lastName, username, email, passwordHash, bio, fileName,
                role, status, createdAt, Instant.now());
    }

    public User withPasswordHash(String hash) {
        return new User(id, firstName, lastName, username, email, hash, bio, profileImage,
                role, status, createdAt, Instant.now());
    }

    public User withStatus(AccountStatus newStatus) {
        return new User(id, firstName, lastName, username, email, passwordHash, bio, profileImage,
                role, newStatus, createdAt, Instant.now());
    }
}
