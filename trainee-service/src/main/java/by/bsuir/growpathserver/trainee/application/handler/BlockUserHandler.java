package by.bsuir.growpathserver.trainee.application.handler;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.BlockUserCommand;
import by.bsuir.growpathserver.trainee.application.exception.IdentityProviderException;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlockUserHandler {

    private final UserRepository userRepository;
    private final IdentityProviderPort identityProviderPort;

    @Transactional
    public User handle(BlockUserCommand command) {
        UserEntity entity = userRepository.findById(command.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + command.userId()));

        try {
            identityProviderPort.setUserEnabled(entity.getKeycloakUserId(), entity.getEmail(), false);
        }
        catch (IdentityProviderException e) {
            throw new IllegalArgumentException("Failed to block user in Keycloak: " + e.getMessage(), e);
        }

        entity.setStatus(UserStatus.BLOCKED);
        UserEntity savedEntity = userRepository.save(entity);
        return User.fromEntity(savedEntity);
    }
}
