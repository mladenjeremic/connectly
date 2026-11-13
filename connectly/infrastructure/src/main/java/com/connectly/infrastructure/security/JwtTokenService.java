package com.connectly.infrastructure.security;
import com.connectly.domain.enums.Role;
import com.connectly.domain.model.*;
import com.connectly.domain.port.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Component
class JwtTokenService implements TokenService {
    private final SecretKey key;
    private final Duration ttl;

    JwtTokenService(@Value("${connectly.jwt.secret}") String secret,
                    @Value("${connectly.jwt.ttl-minutes:30}") long ttlMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public String issue(User u) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(u.id()))
                .claim("role", u.role().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .signWith(key).compact();
    }

    public Optional<TokenClaims> parse(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new TokenClaims(Long.valueOf(c.getSubject()), Role.valueOf(c.get("role", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}