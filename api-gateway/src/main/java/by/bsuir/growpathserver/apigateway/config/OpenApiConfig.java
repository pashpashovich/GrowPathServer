package by.bsuir.growpathserver.apigateway.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                              .title("GrowPath API Gateway")
                              .version("1.0.0")
                              .description("API Gateway for GrowPath Server")
                              .contact(new Contact()
                                               .name("GrowPath Team")
                                               .email("support@growpath.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development server")
                ))
                .components(new Components()
                                    .addSecuritySchemes(securitySchemeName,
                                                        new SecurityScheme()
                                                                .name(securitySchemeName)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description(
                                                                        "JWT токен из Keycloak. Получите токен через /api/v1/auth/login. Введите токен в формате: Bearer <token> или просто <token>")));
    }
}
