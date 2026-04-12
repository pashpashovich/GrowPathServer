package by.bsuir.growpathserver.trainee.application.port;

public interface IdentityProviderPort {

    /**
     * Creates a user in the identity provider and returns the provider user id (Keycloak UUID).
     */
    String createUser(String email, String firstName, String lastName, String roleName);

    void setPassword(String email, String newPassword);

    /**
     * Deletes the user in Keycloak. Uses {@code keycloakUserId} when set; otherwise looks up by {@code email}.
     * If the user is already absent in Keycloak, the call succeeds (idempotent).
     */
    void deleteUser(String keycloakUserId, String email);
}
