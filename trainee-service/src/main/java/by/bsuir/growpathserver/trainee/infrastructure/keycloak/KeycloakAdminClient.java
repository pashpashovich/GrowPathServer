package by.bsuir.growpathserver.trainee.infrastructure.keycloak;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import by.bsuir.growpathserver.trainee.application.exception.IdentityProviderException;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KeycloakAdminClient implements IdentityProviderPort {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.url:http://localhost:8090}")
    private String keycloakUrl;

    @Value("${keycloak.realm:growpath}")
    private String realm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    private String cachedAdminToken;

    private long adminTokenExpiresAt;

    @Override
    public String createUser(String email, String firstName, String lastName, String roleName) {
        String url = baseUrl() + "/admin/realms/" + realm + "/users";

        Map<String, Object> body = Map.of(
                "username", email,
                "email", email,
                "firstName", Objects.nonNull(firstName) ? firstName : "",
                "lastName", Objects.nonNull(lastName) ? lastName : "",
                "enabled", false,
                "emailVerified", false
        );

        ResponseEntity<Void> response = exchangeAdminJson(url, HttpMethod.POST, body, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IdentityProviderException("Failed to create user in Keycloak: " + response.getStatusCode());
        }

        log.info("Created Keycloak user for email={}", email);

        String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        String userId = null;
        if (Objects.nonNull(location)) {
            userId = location.substring(location.lastIndexOf("/") + 1);
        }
        if (StringUtils.isBlank(userId)) {
            userId = findUserByUsername(email);
        }
        if (StringUtils.isBlank(userId)) {
            throw new IdentityProviderException("Keycloak user id not returned for email=" + email);
        }

        if (StringUtils.isNotBlank(roleName)) {
            assignRealmRole(userId, roleName);
        }

        return userId;
    }

    @Override
    public void setPassword(String email, String newPassword) {
        String keycloakUserId = findUserByUsername(email);
        if (StringUtils.isEmpty(keycloakUserId)) {
            log.error("Keycloak user not found for email={}", email);
            throw new IdentityProviderException("Unable to update password. Please try again later.");
        }

        try {
            String resetUrl = baseUrl() + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/reset-password";

            Map<String, Object> passwordBody = Map.of(
                    "type", "password",
                    "value", newPassword,
                    "temporary", false
            );

            log.info("Setting password for userId={}", keycloakUserId);
            ResponseEntity<Void> passwordResponse = exchangeAdminJson(resetUrl, HttpMethod.PUT, passwordBody,
                                                                      Void.class);

            if (!passwordResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to set password in Keycloak: {}", passwordResponse.getStatusCode());
                throw new IdentityProviderException("Unable to update password. Please try again later.");
            }

            String userUrl = baseUrl() + "/admin/realms/" + realm + "/users/" + keycloakUserId;

            Map<String, Object> userBody = Map.of(
                    "enabled", true,
                    "emailVerified", true
            );

            log.info("Setting emailVerified=true for userId={}", keycloakUserId);
            ResponseEntity<Void> userResponse = exchangeAdminJson(userUrl, HttpMethod.PUT, userBody, Void.class);

            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to update emailVerified in Keycloak: {}", userResponse.getStatusCode());
            }

            log.info("Password set and email verified for Keycloak user email={}", email);

        }
        catch (HttpClientErrorException e) {
            log.error("Keycloak error: {}", e.getResponseBodyAsString());
            throw new IdentityProviderException("Unable to update password. Please try again later.");
        }
    }

    @Override
    public void deleteUser(String keycloakUserId, String email) {
        String id = StringUtils.isNotBlank(keycloakUserId) ? keycloakUserId.trim() : findUserByUsername(email);
        if (StringUtils.isBlank(id)) {
            log.warn("No Keycloak user to delete for email={}", email);
            return;
        }

        String url = baseUrl() + "/admin/realms/" + realm + "/users/" + id;
        try {
            ResponseEntity<Void> response = exchangeAdmin(url, HttpMethod.DELETE);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IdentityProviderException("Failed to delete user in Keycloak: " + response.getStatusCode());
            }
            log.info("Deleted Keycloak user id={}", id);
        }
        catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                log.info("Keycloak user already absent id={}", id);
                return;
            }
            log.error("Keycloak delete user failed: {}", e.getResponseBodyAsString());
            throw new IdentityProviderException("Failed to delete user in Keycloak: " + e.getStatusCode(), e);
        }
    }

    private ResponseEntity<Void> exchangeAdmin(String url, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, method, request, Void.class);
    }

    private String findUserByUsername(String username) {
        String url = baseUrl() + "/admin/realms/" + realm + "/users?username=" + username + "&exact=true";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminAccessToken());
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

    private String getAdminAccessToken() {
        if (cachedAdminToken != null && System.currentTimeMillis() < adminTokenExpiresAt) {
            return cachedAdminToken;
        }

        String url = baseUrl() + "/realms/master/protocol/openid-connect/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", "admin-cli");
        params.add("username", adminUsername);
        params.add("password", adminPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {
                });
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        cachedAdminToken = (String) body.get("access_token");
        int expiresIn = ((Number) body.getOrDefault("expires_in", 60)).intValue();
        adminTokenExpiresAt = System.currentTimeMillis() + (expiresIn - 10) * 1000L;
        return cachedAdminToken;
    }

    private <T> ResponseEntity<T> exchangeAdminJson(String url, HttpMethod method, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, method, request, responseType);
    }

    private String baseUrl() {
        return keycloakUrl.endsWith("/") ? keycloakUrl.substring(0, keycloakUrl.length() - 1) : keycloakUrl;
    }

    private void assignRealmRole(String userId, String roleName) {
        String getRoleUrl = baseUrl() + "/admin/realms/" + realm + "/roles/" + roleName;
        ResponseEntity<Map> roleResponse = exchangeAdminJson(getRoleUrl, HttpMethod.GET, null, Map.class);

        if (!roleResponse.getStatusCode().is2xxSuccessful() || roleResponse.getBody() == null) {
            throw new IdentityProviderException("Role not found: " + roleName);
        }

        String assignUrl = baseUrl() + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        List<Map> roles = List.of(roleResponse.getBody());

        ResponseEntity<Void> assignResponse = exchangeAdminJson(assignUrl, HttpMethod.POST, roles, Void.class);

        if (!assignResponse.getStatusCode().is2xxSuccessful()) {
            throw new IdentityProviderException("Failed to assign role: " + assignResponse.getStatusCode());
        }

        log.info("Assigned role '{}' to user {}", roleName, userId);
    }
}
