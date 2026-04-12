package by.bsuir.growpathserver.trainee.application.handler;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.DeleteUserCommand;
import by.bsuir.growpathserver.trainee.application.exception.IdentityProviderException;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteUserHandler {

    private final UserRepository userRepository;
    private final IdentityProviderPort identityProviderPort;

    @Transactional
    public void handle(DeleteUserCommand command) {
        UserEntity user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + command.userId()));

        try {
            identityProviderPort.deleteUser(user.getKeycloakUserId(), user.getEmail());
        }
        catch (IdentityProviderException e) {
            throw new IllegalArgumentException("Failed to delete user in Keycloak: " + e.getMessage(), e);
        }

        userRepository.deleteById(command.userId());
    }
}
