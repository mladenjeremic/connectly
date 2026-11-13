package com.connectly.infrastructure.persistence;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.Role;
import com.connectly.domain.model.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "users")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    // nova polja su nullable da `ddl-auto: update` radi i nad bazom koja već ima korisnike
    @Column(length = 60) public String firstName;
    @Column(length = 60) public String lastName;
    @Column(nullable = false, unique = true, length = 40) public String username;
    @Column(nullable = false, unique = true) public String email;
    @Column(nullable = false) public String passwordHash;
    @Column(length = 300) public String bio;
    @Column(length = 100) public String profileImage;
    @Enumerated(EnumType.STRING) public Role role;
    @Enumerated(EnumType.STRING) public AccountStatus status;
    public Instant createdAt;
    public Instant updatedAt;

    static UserEntity from(User u) {
        UserEntity e = new UserEntity();
        e.id = u.id(); e.firstName = u.firstName(); e.lastName = u.lastName();
        e.username = u.username(); e.email = u.email(); e.passwordHash = u.passwordHash();
        e.bio = u.bio(); e.profileImage = u.profileImage();
        e.role = u.role(); e.status = u.status(); e.createdAt = u.createdAt(); e.updatedAt = u.updatedAt();
        return e;
    }

    User toDomain() {
        return new User(id, firstName == null ? "" : firstName, lastName == null ? "" : lastName,
                username, email, passwordHash, bio == null ? "" : bio, profileImage,
                role, status, createdAt, updatedAt == null ? createdAt : updatedAt);
    }
}
