package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.exception.*;
import com.connectly.domain.model.User;
import com.connectly.domain.port.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Transactional
public class AuthService implements Register, Login {
    /** Ova imena su rezervisana jer se poklapaju sa putanjama API-ja i frontenda. */
    private static final Set<String> RESERVED = Set.of("me", "search", "suggestions", "admin", "api", "login", "register", "feed", "edit", "settings", "notifications", "post");

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenService tokens;
    private final AccountGuard guard;

    public AuthService(UserRepository users, PasswordHasher hasher, TokenService tokens, AccountGuard guard) {
        this.users = users; this.hasher = hasher; this.tokens = tokens; this.guard = guard;
    }

    @Override
    public AuthResult register(RegisterCommand c) {
        String email = c.email().trim().toLowerCase();
        String username = c.username().trim();
        if (RESERVED.contains(username.toLowerCase())) throw new ConflictException("Korisničko ime je zauzeto.");
        if (users.existsByEmail(email)) throw new ConflictException("Email adresa je već u upotrebi.");
        if (users.existsByUsername(username)) throw new ConflictException("Korisničko ime je zauzeto.");
        User saved = users.save(User.register(c.firstName().trim(), c.lastName().trim(), username, email,
                hasher.hash(c.password())));
        return new AuthResult(tokens.issue(saved), saved.id(), saved.username(), saved.role());
    }

    @Override
    public AuthResult login(LoginCommand c) {
        User found = users.findByEmail(c.email().trim().toLowerCase())
                .filter(x -> hasher.matches(c.password(), x.passwordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Pogrešan email ili lozinka."));
        User u = guard.refresh(found);
        if (!u.isActive()) throw new AccountBlockedException(guard.describeBlock(u));
        return new AuthResult(tokens.issue(u), u.id(), u.username(), u.role());
    }
}
