package com.connectly.domain.port;
import com.connectly.domain.model.TokenClaims;
import com.connectly.domain.model.User;
import java.util.Optional;
public interface TokenService {
    String issue(User user);
    Optional<TokenClaims> parse(String token);
}