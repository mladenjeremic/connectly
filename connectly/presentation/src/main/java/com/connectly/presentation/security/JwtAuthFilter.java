package com.connectly.presentation.security;

import com.connectly.application.UseCases.ResolveAccount;
import com.connectly.domain.port.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final TokenService tokens;
    private final ResolveAccount accounts;

    public JwtAuthFilter(TokenService tokens, ResolveAccount accounts) {
        this.tokens = tokens; this.accounts = accounts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            // Rola dolazi iz baze, ne iz tokena i ne od frontenda. Ban/suspenzija važe odmah,
            // a istekla privremena zabrana se ovde automatski skida.
            tokens.parse(header.substring(7))
                    .flatMap(c -> accounts.resolve(c.userId()))
                    .ifPresent(u -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    u.id(), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + u.role().name())))));
        }
        chain.doFilter(req, res);
    }
}
