package com.cakedelight.rating.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Resolves display usernames from user-service using the existing internal
 * service-to-service pattern (RestClient + X-Internal-Secret), mirroring
 * order-service's BasketServiceImpl -> catalog-service lookup.
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    private static final String FALLBACK_USERNAME = "Customer";

    private final RestClient userClient;

    public UserServiceClient(
            @Value("${user.service.base-url:http://user-service:8085}") String userServiceBaseUrl,
            @Value("${gateway.internal-secret:c2VjcmV0LWtleS1jYWtlLWRlbGlnaHQtdjItc3VwZXItc2VjcmV0LXNlY3JldC1rZXk=}") String internalSecret) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);

        this.userClient = RestClient.builder()
                .baseUrl(userServiceBaseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Secret", internalSecret)
                .build();
    }

    public String getUsername(Long userId) {
        if (userId == null) {
            return FALLBACK_USERNAME;
        }
        try {
            UserLookupResponse response = userClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, httpResponse) -> {
                        throw new IllegalStateException("User not found: " + userId);
                    })
                    .body(UserLookupResponse.class);
            return (response != null && response.username() != null) ? response.username() : FALLBACK_USERNAME;
        } catch (Exception ex) {
            log.warn("Failed to resolve username for userId {}: {}", userId, ex.getMessage());
            return FALLBACK_USERNAME;
        }
    }

    private record UserLookupResponse(Long id, String username) {
    }
}
