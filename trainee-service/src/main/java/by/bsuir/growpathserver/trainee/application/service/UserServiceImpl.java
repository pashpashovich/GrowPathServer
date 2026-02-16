package by.bsuir.growpathserver.trainee.application.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.CreateUserCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.events.UserCreatedEvent;
import by.bsuir.growpathserver.trainee.infrastructure.keycloak.KeycloakAdminClient;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    @Transactional
    public User createUser(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("User with email " + command.email() + " already exists");
        }

        User user = User.create(
                command.email(),
                command.name(),
                command.role(),
                command.invitedBy()
        );

        UserEntity entity = user.toEntity();
        UserEntity savedEntity = userRepository.save(entity);
        User savedUser = User.fromEntity(savedEntity);

        String temporaryPassword = UUID.randomUUID().toString().replace("-", "") + "A1a!";
        try {
            keycloakAdminClient.createUser(
                    savedUser.getEmail().value(),
                    savedUser.getName(),
                    temporaryPassword
            );
        } catch (Exception e) {
            log.warn("Failed to create user in Keycloak (user created in DB): {}", e.getMessage());
            throw new IllegalArgumentException("Failed to create user in Keycloak: " + e.getMessage(), e);
        }

        UserCreatedEvent event = new UserCreatedEvent(
                savedUser.getId(),
                savedUser.getEmail().value(),
                savedUser.getName(),
                savedUser.getRole(),
                savedUser.getInvitedBy(),
                savedUser.getCreatedAt()
        );
        eventPublisher.publishEvent(event);

        return savedUser;
    }
}
