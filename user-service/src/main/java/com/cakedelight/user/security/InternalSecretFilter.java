package com.cakedelight.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class InternalSecretFilter extends OncePerRequestFilter {

    @Value("${gateway.internal-secret:c2VjcmV0LWtleS1jYWtlLWRlbGlnaHQtdjItc3VwZXItc2VjcmV0LXNlY3JldC1rZXk=}")
    private String expectedSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Allow actuator probes and health checks
        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String incomingSecret = request.getHeader("X-Internal-Secret");

        if (incomingSecret == null || !expectedSecret.equals(incomingSecret)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String jsonResponse = String.format(
                    "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Missing or invalid internal gateway secret\"}",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            );
            response.getWriter().write(jsonResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
