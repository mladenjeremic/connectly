package com.connectly.presentation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jednostavno ograničenje učestalosti po IP adresi (u memoriji, jedan server):
 * strože za prijavu/registraciju (zaštita od pogađanja lozinki), blaže za ostatak API-ja.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private record Bucket(long windowStart, int count) {}

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int authLimit;
    private final int apiLimit;
    private static final long WINDOW_MS = 60_000;

    public RateLimitFilter(@Value("${connectly.rate-limit.auth-per-minute:20}") int authLimit,
                           @Value("${connectly.rate-limit.api-per-minute:600}") int apiLimit) {
        this.authLimit = authLimit; this.apiLimit = apiLimit;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/") || uri.startsWith("/api/images/") || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        boolean auth = req.getRequestURI().startsWith("/api/auth/");
        int limit = auth ? authLimit : apiLimit;
        String key = (auth ? "a:" : "p:") + req.getRemoteAddr();
        long now = System.currentTimeMillis();
        Bucket b = buckets.merge(key, new Bucket(now, 1),
                (old, fresh) -> now - old.windowStart() >= WINDOW_MS ? fresh : new Bucket(old.windowStart(), old.count() + 1));
        if (b.count() > limit) {
            res.setStatus(429);
            res.setHeader("Retry-After", "60");
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"message\":\"Previše zahteva. Pokušaj ponovo za minut.\"}");
            return;
        }
        if (buckets.size() > 50_000) buckets.entrySet().removeIf(e -> now - e.getValue().windowStart() >= WINDOW_MS);
        chain.doFilter(req, res);
    }
}
