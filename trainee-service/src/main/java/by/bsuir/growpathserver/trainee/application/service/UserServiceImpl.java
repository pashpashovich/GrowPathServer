package by.bsuir.growpathserver.trainee.application.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.CreateUserCommand;
import by.bsuir.growpathserver.trainee.application.exception.IdentityProviderException;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.events.UserCreatedEvent;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IdentityProviderPort identityProviderPort;

    @Override
    @Transactional
    public User createUser(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("User with email " + command.email() + " already exists");
        }

        User user = User.create(
                command.email(),
                command.firstName(),
                command.lastName(),
                command.patronymicName(),
                command.role(),
                command.invitedBy()
        );

        UserEntity entity = user.toEntity();
        UserEntity savedEntity = userRepository.save(entity);
        User savedUser = User.fromEntity(savedEntity);

        String keycloakUserId;
        try {
            keycloakUserId = identityProviderPort.createUser(
                    savedUser.getEmail().value(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    String.valueOf(savedUser.getRole())
            );
        }
        catch (IdentityProviderException e) {
            log.warn("Failed to create user in identity provider — transaction will roll back DB user: {}",
                     e.getMessage());
            throw new IllegalArgumentException("Failed to create user in Keycloak: " + e.getMessage(), e);
        }
        catch (RuntimeException e) {
            log.warn("Failed to create user in identity provider — transaction will roll back DB user: {}",
                     e.getMessage());
            throw new IllegalArgumentException("Failed to create user in Keycloak: " + e.getMessage(), e);
        }

        UserEntity linked = userRepository.findById(savedUser.getId()).orElseThrow();
        linked.setKeycloakUserId(keycloakUserId);
        UserEntity persisted = userRepository.save(linked);
        User result = User.fromEntity(persisted);

        UserCreatedEvent event = new UserCreatedEvent(
                result.getId(),
                result.getEmail().value(),
                result.getFirstName(),
                result.getLastName(),
                result.getPatronymicName(),
                result.getRole(),
                result.getInvitedBy(),
                result.getCreatedAt()
        );
        eventPublisher.publishEvent(event);

        return result;
    }
}
