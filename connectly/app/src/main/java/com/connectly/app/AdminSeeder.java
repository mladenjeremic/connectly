package com.connectly.app;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.Role;
import com.connectly.domain.model.User;
import com.connectly.domain.port.PasswordHasher;
import com.connectly.domain.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.time.Instant;

/**
 * Pri prvom pokretanju pravi administratorski nalog (jedini način da se dobije ADMIN rola,
 * jer registracija uvek pravi običnog korisnika). Isključuje se praznim connectly.admin.email.
 */
@Component
class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final String email;
    private final String username;
    private final String password;

    AdminSeeder(UserRepository users, PasswordHasher hasher,
                @Value("${connectly.admin.email:}") String email,
                @Value("${connectly.admin.username:admin}") String username,
                @Value("${connectly.admin.password:}") String password) {
        this.users = users; this.hasher = hasher; this.email = email; this.username = username; this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) return;
        String mail = email.trim().toLowerCase();
        if (users.existsByEmail(mail) || users.existsByUsername(username)) return;
        Instant now = Instant.now();
        users.save(new User(null, "Admin", "Connectly", username, mail, hasher.hash(password), "", null,
                Role.ADMIN, AccountStatus.ACTIVE, now, now));
        log.warn("Kreiran administratorski nalog '{}' ({}). Promeni lozinku pre bilo kakvog deploy-a!", username, mail);
    }
}
