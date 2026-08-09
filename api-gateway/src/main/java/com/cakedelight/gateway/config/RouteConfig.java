package com.cakedelight.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("ui-root", r -> r.path("/")
                        .filters(f -> f.setPath("/index.html"))
                        .uri("forward:/"))
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
                .route("catalog-root", r -> r.path("/api/catalog")
                        .filters(f -> f.rewritePath("/api/catalog", "/api/cakes"))
                        .uri("http://catalog-service:8081"))
                .route("catalog-cakes", r -> r.path("/api/catalog/cakes")
                        .filters(f -> f.rewritePath("/api/catalog/cakes", "/api/cakes"))
                        .uri("http://catalog-service:8081"))
                .route("catalog-resource", r -> r.path("/api/catalog/**")
                        .filters(f -> f.rewritePath("/api/catalog/(?<remaining>.*)", "/api/cakes/${remaining}"))
                        .uri("http://catalog-service:8081"))
                .route("orders", r -> r.path("/api/orders/**")
                        .filters(f -> f.rewritePath("/api/orders/(?<remaining>.*)", "/api/orders/${remaining}"))
                        .uri("http://order-service:8082"))
                .route("basket", r -> r.path("/api/basket/**")
                        .filters(f -> f.rewritePath("/api/basket/(?<remaining>.*)", "/api/basket/${remaining}"))
                        .uri("http://order-service:8082"))
                .route("checkout", r -> r.path("/api/checkout")
                        .uri("http://order-service:8082"))
                .route("ratings", r -> r.path("/api/ratings/**")
                        .filters(f -> f.rewritePath("/api/ratings/(?<remaining>.*)", "/api/ratings/${remaining}"))
                        .uri("http://rating-service:8083"))
                .route("notifications", r -> r.path("/api/notifications/**")
                        .filters(f -> f.rewritePath("/api/notifications/(?<remaining>.*)", "/api/notifications/${remaining}"))
                        .uri("http://notification-service:8084"))
                .route("health", r -> r.path("/actuator/health")
                        .uri("http://catalog-service:8081"))
                .build();
    }
}
