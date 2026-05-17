package by.bsuir.growpathserver.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                              .title("GrowPath Notification Service API")
                              .version("1.0.0")
                              .description(
                                      "Authentication: 1) Obtain a token via POST /api/v1/auth/login on the API Gateway "
                                              + "(http://localhost:8080), "
                                              + "2) Click Authorize above, 3) Enter the token as Bearer <token> or just <token>"))
                .components(new Components()
                                    .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                            .name(securitySchemeName)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")
                                            .description(
                                                    "JWT from Keycloak. Obtain via POST /api/v1/auth/login on the API Gateway")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
