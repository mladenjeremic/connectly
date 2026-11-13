package com.connectly.infrastructure.security;
import com.connectly.domain.port.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BcryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
    public String hash(String raw) { return enc.encode(raw); }
    public boolean matches(String raw, String hash) { return enc.matches(raw, hash); }
}