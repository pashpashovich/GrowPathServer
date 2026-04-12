package by.bsuir.growpathserver.trainee.application.port;

public interface IdentityProviderPort {

    String createUser(String email, String firstName, String lastName, String roleName);

    void setPassword(String email, String newPassword);

    void deleteUser(String keycloakUserId, String email);

    void setUserEnabled(String keycloakUserId, String email, boolean enabled);
}
