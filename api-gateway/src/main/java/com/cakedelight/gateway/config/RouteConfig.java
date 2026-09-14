package com.cakedelight.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    private static final Logger log = LoggerFactory.getLogger(RouteConfig.class);

    @Value("${USER_SERVICE_URL:http://host.docker.internal:8085}")
    private String userServiceUrl;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        log.info("Configuring user-service route with URI: {}", userServiceUrl);
        return builder.routes()
                .route("ui-root", r -> r.path("/")
                        .filters(f -> f.setPath("/index.html"))
                        .uri("forward:/"))
                .route("catalog-root", r -> r.path("/api/catalog")
                        .filters(f -> f.rewritePath("/api/catalog", "/api/cakes"))
                        .uri("http://catalog-service:8081"))
                .route("catalog-cakes", r -> r.path("/api/catalog/cakes", "/api/catalog/cakes/**")
                        .filters(f -> f.rewritePath("/api/catalog/cakes(?<remaining>/?.*)", "/api/cakes${remaining}"))
                        .uri("http://catalog-service:8081"))
                .route("catalog-resource", r -> r.path("/api/catalog/**")
                        .filters(f -> f.rewritePath("/api/catalog/(?<remaining>.*)", "/api/cakes/${remaining}"))
                        .uri("http://catalog-service:8081"))
                .route("orders-basket", r -> r.path("/api/orders/basket", "/api/orders/basket/**")
                        .filters(f -> f.rewritePath("/api/orders/basket(?<remaining>/?.*)", "/api/basket${remaining}"))
                        .uri("http://order-service:8082"))
                .route("orders-checkout", r -> r.path("/api/orders/checkout")
                        .filters(f -> f.rewritePath("/api/orders/checkout", "/api/checkout"))
                        .uri("http://order-service:8082"))
                .route("orders", r -> r.path("/api/orders", "/api/orders/**")
                        .filters(f -> f.rewritePath("/api/orders(?<remaining>/?.*)", "/api/orders${remaining}"))
                        .uri("http://order-service:8082"))
                .route("basket", r -> r.path("/api/basket/**")
                        .filters(f -> f.rewritePath("/api/basket/(?<remaining>.*)", "/api/basket${remaining}"))
                        .uri("http://order-service:8082"))
                .route("checkout", r -> r.path("/api/checkout")
                        .uri("http://order-service:8082"))
                .route("ratings-cakes-average", r -> r.path("/api/ratings/cakes/*/average")
                        .filters(f -> f.rewritePath("/api/ratings/cakes/(?<id>[^/]+)/average", "/api/cakes/${id}/ratings/average"))
                        .uri("http://rating-service:8083"))
                .route("ratings-cakes-list", r -> r.path("/api/ratings/cakes/*")
                        .filters(f -> f.rewritePath("/api/ratings/cakes/(?<id>[^/]+)", "/api/cakes/${id}/ratings"))
                        .uri("http://rating-service:8083"))
                .route("ratings", r -> r.path("/api/ratings/**")
                        .filters(f -> f.rewritePath("/api/ratings/(?<remaining>.*)", "/api/ratings/${remaining}"))
                        .uri("http://rating-service:8083"))
                .route("notifications", r -> r.path("/api/notifications/**")
                        .filters(f -> f.rewritePath("/api/notifications/(?<remaining>.*)", "/api/notifications/${remaining}"))
                        .uri("http://notification-service:8084"))
                .route("user-service", r -> r.path("/api/auth/**")
                        .uri(userServiceUrl))
                .route("health", r -> r.path("/actuator/health")
                        .uri("http://catalog-service:8081"))
                .route("ui-fallback", r -> r.path("/**")
                        .and()
                        .predicate(exchange -> {
                            String path = exchange.getRequest().getURI().getPath();
                            return !path.startsWith("/api/")
                                    && !path.startsWith("/actuator/")
                                    && !path.contains(".");
                        })
                        .filters(f -> f.setPath("/index.html"))
                        .uri("forward:/"))
                .build();
    }
}
