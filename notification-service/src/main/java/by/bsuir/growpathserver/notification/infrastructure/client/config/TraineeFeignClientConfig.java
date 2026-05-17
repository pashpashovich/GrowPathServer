package by.bsuir.growpathserver.notification.infrastructure.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import by.bsuir.growpathserver.notification.application.exception.TraineeServiceCommunicationException;
import by.bsuir.growpathserver.notification.application.exception.TraineeUserNotFoundException;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;

public class TraineeFeignClientConfig {

    @Bean
    public RequestInterceptor traineeServiceAuthInterceptor() {
        return template -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                template.header("Authorization", "Bearer " + jwtAuth.getToken().getTokenValue());
            }
        };
    }

    @Bean
    public ErrorDecoder traineeFeignErrorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new TraineeUserNotFoundException();
            }
            return new TraineeServiceCommunicationException(response.status());
        };
    }
}
