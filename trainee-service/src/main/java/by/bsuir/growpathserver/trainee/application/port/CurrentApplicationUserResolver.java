package by.bsuir.growpathserver.trainee.application.port;

import java.util.Optional;

public interface CurrentApplicationUserResolver {

    Optional<Long> resolveCurrentUserDatabaseId();

    Optional<String> resolveCurrentKeycloakSubject();
}
