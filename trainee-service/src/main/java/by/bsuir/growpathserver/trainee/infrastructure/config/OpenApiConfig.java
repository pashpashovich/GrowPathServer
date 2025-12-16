package by.bsuir.growpathserver.trainee.infrastructure.config;

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
                              .title("GrowPath Trainee Service API")
                              .version("1.0.0")
                              .description(
                                      "Для авторизации: 1) Получите токен через POST /api/v1/auth/login в API Gateway (http://localhost:8080), "
                                              +
                                              "2) Нажмите кнопку 'Authorize' выше, 3) Введите токен в формате: Bearer <token> или просто <token>"))
                .components(new Components()
                                    .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                            .name(securitySchemeName)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")
                                            .description(
                                                    "JWT токен из Keycloak. Получите через POST /api/v1/auth/login в API Gateway")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
