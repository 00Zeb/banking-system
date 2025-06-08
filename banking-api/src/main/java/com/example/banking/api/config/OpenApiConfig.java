package com.example.banking.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking REST API")
                        .description("REST API wrapper for the Banking Core Application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Banking API Team")
                                .email("api@banking.example.com")));
    }
}
