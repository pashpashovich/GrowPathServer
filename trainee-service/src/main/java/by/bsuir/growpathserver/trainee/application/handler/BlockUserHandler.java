package by.bsuir.growpathserver.trainee.application.handler;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.common.model.kafka.UserBlockedEvent;
import by.bsuir.growpathserver.trainee.application.command.BlockUserCommand;
import by.bsuir.growpathserver.trainee.application.exception.IdentityProviderException;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockUserHandler {

    @Value("${kafka.topic.user-blocked:USER_BLOCKED}")
    private String topicUserBlocked;

    private final UserRepository userRepository;
    private final IdentityProviderPort identityProviderPort;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        User user = User.fromEntity(savedEntity);

        sendUserBlockedEvent(user);
        return user;
    }

    private void sendUserBlockedEvent(User user) {
        try {
            UserBlockedEvent event = new UserBlockedEvent(
                    String.valueOf(user.getId()),
                    user.getEmail().value(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPatronymicName()
            );
            kafkaTemplate.send(topicUserBlocked, user.getId().toString(), event);
            log.info("Sent UserBlockedEvent for user id={}, email={}", user.getId(), user.getEmail().value());
        }
        catch (Exception e) {
            log.warn("Failed to send UserBlockedEvent for user id={}, email will not be sent: {}",
                     user.getId(), e.getMessage());
        }
    }
}
