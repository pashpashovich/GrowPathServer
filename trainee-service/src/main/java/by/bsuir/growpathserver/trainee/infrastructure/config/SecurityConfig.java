package by.bsuir.growpathserver.trainee.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import by.bsuir.growpathserver.common.security.GrowPathResourceServerJwt;
import by.bsuir.growpathserver.trainee.infrastructure.security.LastLoginUpdateFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return GrowPathResourceServerJwt.authenticationConverter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter,
                                                   LastLoginUpdateFilter lastLoginUpdateFilter)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/trainee/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                         "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/v1/auth/complete-registration",
                                         "/api/v1/auth/forgot-password",
                                         "/api/v1/auth/reset-password").permitAll()
                        .requestMatchers("/trainee/**", "/api/users/**", "/api/v1/**", "/tasks/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                                                                   jwt.jwtAuthenticationConverter(
                                                                           jwtAuthenticationConverter)))
                .addFilterAfter(lastLoginUpdateFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}
