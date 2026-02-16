package by.bsuir.growpathserver.trainee.infrastructure.keycloak;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.url:http://localhost:8090}")
    private String keycloakUrl;

    @Value("${keycloak.realm:growpath}")
    private String realm;

    @Value("${keycloak.client.id:trainee-service}")
    private String clientId;

    @Value("${keycloak.client.secret:trainee-service-secret}")
    private String clientSecret;

    private String cachedToken;
    private long tokenExpiresAt;

    public void createUser(String email, String name, String temporaryPassword) {
        String token = getAccessToken();
        String url = baseUrl() + "/admin/realms/" + realm + "/users";

        Map<String, Object> body = Map.of(
                "username", email,
                "email", email,
                "firstName", name != null ? name : "",
                "enabled", true,
                "emailVerified", false,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", temporaryPassword,
                        "temporary", true
                )),
                "requiredActions", List.of("UPDATE_PASSWORD")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Created Keycloak user for email={}", email);
        }
        else {
            throw new KeycloakAdminException("Failed to create user in Keycloak: " + response.getStatusCode());
        }
    }

    public void setPassword(String email, String newPassword) {
        String keycloakUserId = findUserByUsername(email);
        if (keycloakUserId == null) {
            throw new KeycloakAdminException("User not found in Keycloak: " + email);
        }

        String token = getAccessToken();
        String url = baseUrl() + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/reset-password";

        Map<String, Object> body = Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new KeycloakAdminException("Failed to set password in Keycloak: " + response.getStatusCode());
        }
        log.info("Password set for Keycloak user email={}", email);
    }

    @SuppressWarnings("unchecked")
    private String findUserByUsername(String username) {
        String token = getAccessToken();
        String url = baseUrl() + "/admin/realms/" + realm + "/users?username=" + username + "&exact=true";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {
                });
        if (response.getBody() == null || response.getBody().isEmpty()) {
            return null;
        }
        Map<String, Object> user = response.getBody().get(0);
        return (String) user.get("id");
    }

    private String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return cachedToken;
        }

        String url = baseUrl() + "/realms/" + realm + "/protocol/openid-connect/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {
                });
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        cachedToken = (String) body.get("access_token");
        int expiresIn = ((Number) body.getOrDefault("expires_in", 60)).intValue();
        tokenExpiresAt = System.currentTimeMillis() + (expiresIn - 10) * 1000L;
        return cachedToken;
    }

    private String baseUrl() {
        return keycloakUrl.endsWith("/") ? keycloakUrl.substring(0, keycloakUrl.length() - 1) : keycloakUrl;
    }

    public static class KeycloakAdminException extends RuntimeException {
        public KeycloakAdminException(String message) {
            super(message);
        }
    }
}
