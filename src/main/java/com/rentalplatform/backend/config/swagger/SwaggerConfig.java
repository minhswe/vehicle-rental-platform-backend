package com.rentalplatform.backend.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                              .title("Vehicle Rental Platform Backend")
                              .version("1.0")
                              .description("""
            REST API documentation for Vehicle Rental Platform.
            Available Modules:
            - Authentication & Authorization
            - Owner Management
            - Vehicle Management
            - Vehicle Image Management
            - Vehicle Document Management
            - Admin could approve/reject vehicle documents
            """)
                              .contact(
                                      new Contact()
                                              .name("mr. Pham Quang Minh")
                                              .email("quangminhswe@gmail.com")
                              )
                              .license(
                                      new License()
                                              .name("MIT License")
                              )

                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development")
                ))
                //apply security for all api
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )
                //declare bearer jwt
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(SECURITY_SCHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("Input JWT access token")
                                )
                );


    }
}
