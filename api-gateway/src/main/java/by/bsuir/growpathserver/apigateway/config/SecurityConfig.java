package by.bsuir.growpathserver.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import by.bsuir.growpathserver.common.security.GrowPathResourceServerJwt;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter reactiveJwtAuthenticationConverter() {
        return new ReactiveJwtAuthenticationConverterAdapter(GrowPathResourceServerJwt.authenticationConverter());
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtAuthenticationConverterAdapter reactiveJwtAuthenticationConverter) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**", "/health").permitAll()
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/index.html",
                                      "/webjars/**").permitAll()
                        .pathMatchers("/api/v1/auth/login", "/api/v1/auth/logout", "/api/v1/auth/refresh",
                                      "/api/v1/auth/complete-registration", "/api/v1/auth/forgot-password",
                                      "/api/v1/auth/reset-password").permitAll()
                        .pathMatchers("/api/v1/storage/**").permitAll()
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/v1/auth/user", "/api/v1/auth/validate").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                                                                   jwt.jwtAuthenticationConverter(
                                                                           reactiveJwtAuthenticationConverter)));

        return http.build();
    }
}
