package com.shopmate.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * Runs on every request. If the Authorization header carries a valid JWT,
 * the filter extracts userId and role from the token and injects them as
 * X-User-Id, X-User-Email and X-User-Role headers for downstream services.
 *
 * Public paths (register, login, product browsing, etc.) pass through without a token.
 * Protected paths that have no valid token receive a 401.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/products",
            "/api/categories",
            "/api/chatbot",
            "/actuator"
    );

    private final JwtTokenParser jwtTokenParser;

    public JwtAuthFilter(JwtTokenParser jwtTokenParser) {
        this.jwtTokenParser = jwtTokenParser;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // Try to parse the token whether the path is public or not (enriching headers for downstream)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtTokenParser.parse(token);
                String email = claims.getSubject();
                Object userId = claims.get("userId");
                Object role = claims.get("role");

                // Wrap the request to inject headers
                HttpServletRequest enriched = addHeaders(request, Map.of(
                        "X-User-Id", userId != null ? userId.toString() : "",
                        "X-User-Email", email != null ? email : "",
                        "X-User-Role", role != null ? role.toString() : ""
                ));

                filterChain.doFilter(enriched, response);
                return;
            } catch (JwtException e) {
                log.debug("Invalid JWT: {}", e.getMessage());
                // If this is a protected path, reject it
                if (!isPublicPath(path)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                    return;
                }
                // Public path => fall through without user context
            }
        } else {
            // No token provided
            if (!isPublicPath(path)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authorization header is required\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private HttpServletRequest addHeaders(HttpServletRequest request, Map<String, String> extra) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (extra.containsKey(name)) {
                    return extra.get(name);
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (extra.containsKey(name)) {
                    return Collections.enumeration(List.of(extra.get(name)));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> names = new LinkedHashSet<>();
                Enumeration<String> original = super.getHeaderNames();
                while (original.hasMoreElements()) {
                    names.add(original.nextElement());
                }
                names.addAll(extra.keySet());
                return Collections.enumeration(names);
            }
        };
    }
}
