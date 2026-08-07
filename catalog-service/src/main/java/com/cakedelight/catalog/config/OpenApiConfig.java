package com.cakedelight.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cakeDelightOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Cake Delight Catalog Service API")
                        .description("REST APIs for managing cakes in the Cake Delight application.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Prathibha M")
                                .email("prathibha@example.com")));
    }
}