package by.bsuir.growpathserver.apigateway.service;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import by.bsuir.growpathserver.apigateway.dto.model.auth.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Service
public class AuthService {

    private final WebClient webClient;
    private final String keycloakUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    public AuthService(
            WebClient.Builder webClientBuilder,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${keycloak.client.id:api-gateway}") String clientId,
            @Value("${keycloak.client.secret:api-gateway-secret}") String clientSecret) {
        this.webClient = webClientBuilder.build();

        if (issuerUri.contains("/realms/")) {
            int realmsIndex = issuerUri.indexOf("/realms/");
            this.keycloakUrl = issuerUri.substring(0, realmsIndex);
            this.realm = issuerUri.substring(realmsIndex + "/realms/".length());
        }
        else {
            this.keycloakUrl = "http://localhost:8090";
            this.realm = "growpath";
        }
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public Mono<TokenResponse> login(String username, String password) {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                                        keycloakUrl, realm);

        log.debug("Attempting to authenticate user: {} against Keycloak at: {}", username, tokenUrl);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", username);
        formData.add("password", password);

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("Keycloak authentication failed with status: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Keycloak error response: {}", body);
                                return Mono.error(new RuntimeException(
                                        "Authentication failed: " + response.statusCode() + " - " + body));
                            });
                })
                .bodyToMono(Map.class)
                .map(this::mapKeycloakResponseToTokenResponse)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                                   .filter(throwable -> throwable instanceof WebClientResponseException &&
                                           ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                                   .doBeforeRetry(retrySignal ->
                                                          log.warn("Retrying Keycloak authentication, attempt: {}",
                                                                   retrySignal.totalRetries() + 1)))
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> log.debug("Authentication successful for user: {}", username))
                .doOnError(error -> log.error("Authentication error for user: {}", username, error))
                .onErrorMap(ex -> {
                    if (ex instanceof java.util.concurrent.TimeoutException) {
                        return new RuntimeException("Keycloak connection timeout. Is Keycloak running?", ex);
                    }
                    if (ex instanceof org.springframework.web.reactive.function.client.WebClientException) {
                        return new RuntimeException(
                                "Failed to connect to Keycloak. Check if Keycloak is accessible at: " + keycloakUrl,
                                ex);
                    }
                    return new RuntimeException("Failed to authenticate user: " + ex.getMessage(), ex);
                });
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                                        keycloakUrl, realm);

        log.debug("Attempting to refresh token against Keycloak at: {}", tokenUrl);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("Keycloak token refresh failed with status: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Keycloak error response: {}", body);
                                return Mono.error(new RuntimeException(
                                        "Token refresh failed: " + response.statusCode() + " - " + body));
                            });
                })
                .bodyToMono(Map.class)
                .map(this::mapKeycloakResponseToTokenResponse)
                .timeout(Duration.ofSeconds(30))
                .doOnError(error -> log.error("Token refresh error", error))
                .onErrorMap(ex -> {
                    if (ex instanceof java.util.concurrent.TimeoutException) {
                        return new RuntimeException("Keycloak connection timeout", ex);
                    }
                    return new RuntimeException("Failed to refresh token: " + ex.getMessage(), ex);
                });
    }

    public Mono<Void> logout(String refreshToken) {
        String logoutUrl = String.format("%s/realms/%s/protocol/openid-connect/logout",
                                         keycloakUrl, realm);

        log.debug("Attempting to logout against Keycloak at: {}", logoutUrl);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        return webClient.post()
                .uri(logoutUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.warn("Keycloak logout returned error status: {}", response.statusCode());
                    return Mono.empty(); // Logout is best-effort, don't fail on error
                })
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(error -> log.warn("Logout error (non-critical)", error))
                .onErrorResume(ex -> {
                    log.warn("Logout failed, but continuing anyway", ex);
                    return Mono.empty(); // Don't fail on logout errors
                });
    }

    public String getAuthorizationUrl(String redirectUri) {
        return String.format(
                "%s/realms/%s/protocol/openid-connect/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=openid profile email roles",
                keycloakUrl, realm, clientId, redirectUri);
    }

    private TokenResponse mapKeycloakResponseToTokenResponse(Map<String, Object> keycloakResponse) {
        TokenResponse response = new TokenResponse();

        Object accessToken = keycloakResponse.get("access_token");
        Object refreshToken = keycloakResponse.get("refresh_token");

        if (accessToken != null) {
            response.setAccessToken(accessToken.toString());
        }
        if (refreshToken != null) {
            response.setRefreshToken(refreshToken.toString());
        }

        log.debug("Mapped Keycloak response to TokenResponse. Access token present: {}, Refresh token present: {}",
                  accessToken != null, refreshToken != null);

        return response;
    }
}
