package com.cakedelight.gateway.filter;

import com.cakedelight.gateway.security.JwtValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtValidator jwtValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/actuator/**"
    );

    @org.springframework.beans.factory.annotation.Value("${gateway.internal-secret:c2VjcmV0LWtleS1jYWtlLWRlbGlnaHQtdjItc3VwZXItc2VjcmV0LXNlY3JldC1rZXk=}")
    private String internalSecret;

    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 1. Check if public route
        if (isPublicRoute(path, method)) {
            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(httpHeaders -> httpHeaders.remove("X-Internal-Secret"))
                    .header("X-Internal-Secret", internalSecret)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // 2. Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Bearer token is empty");
        }

        // 3. Validate Token
        try {
            Claims claims = jwtValidator.extractAllClaims(token);

            Object userIdObj = claims.get("userId");
            String userId = userIdObj != null ? userIdObj.toString() : "";
            String role = claims.get("role", String.class);
            if (role == null) role = "";
            String username = claims.getSubject();
            if (username == null) username = "";

            // 4. Remove untrusted client-supplied headers and add verified headers
            if (requiresAdminRole(path, method)) {
                if (!"ROLE_ADMIN".equals(role)) {
                    return onError(exchange, HttpStatus.FORBIDDEN, "Access denied: Requires ROLE_ADMIN");
                }
            } else if (!isUserOrAdminAuthorized(role)) {
                return onError(exchange, HttpStatus.FORBIDDEN, "Access denied: Invalid or insufficient role");
            }

            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(httpHeaders -> {
                        httpHeaders.remove("X-Internal-Secret");
                        httpHeaders.remove("X-User-Id");
                        httpHeaders.remove("X-User-Role");
                        httpHeaders.remove("X-User-Name");
                    })
                    .header("X-Internal-Secret", internalSecret)
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .header("X-User-Name", username)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException ex) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "JWT token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }
    }

    private boolean isPublicRoute(String path, HttpMethod method) {
        for (String publicPattern : PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(publicPattern, path)) {
                return true;
            }
        }

        if (HttpMethod.GET.equals(method)) {
            if (pathMatcher.match("/api/catalog/**", path) ||
                pathMatcher.match("/api/cakes/**", path) ||
                pathMatcher.match("/api/ratings/cakes/**", path)) {
                return true;
            }
        }

        if (!path.startsWith("/api/") && !path.startsWith("/actuator/")) {
            return true;
        }

        return false;
    }

    private boolean requiresAdminRole(String path, HttpMethod method) {
        if (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.DELETE.equals(method)) {
            return pathMatcher.match("/api/catalog/cakes", path) ||
                   pathMatcher.match("/api/catalog/cakes/**", path) ||
                   pathMatcher.match("/api/cakes", path) ||
                   pathMatcher.match("/api/cakes/**", path);
        }
        return false;
    }

    private boolean isUserOrAdminAuthorized(String role) {
        return "ROLE_USER".equals(role) || "ROLE_ADMIN".equals(role);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                status.value(),
                status.getReasonPhrase(),
                message
        );

        DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
