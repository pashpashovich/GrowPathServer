package by.bsuir.growpathserver.trainee.application.handler;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.common.model.kafka.UserInvitedEvent;
import by.bsuir.growpathserver.trainee.application.command.InviteUserCommand;
import by.bsuir.growpathserver.trainee.application.service.RegistrationTokenService;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteUserHandler {

    @Value("${kafka.topic.user-invited:USERINVITED}")
    private String topicUserInvited;

    private final UserRepository userRepository;
    private final RegistrationTokenService registrationTokenService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public User handle(InviteUserCommand command) {
        UserEntity entity = userRepository.findById(command.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + command.userId()));

        entity.setInvitationSentAt(LocalDateTime.now());
        UserEntity savedEntity = userRepository.save(entity);
        User user = User.fromEntity(savedEntity);

        sendInvitationEmailEvent(user);
        return user;
    }

    private void sendInvitationEmailEvent(User user) {
        try {
            String registrationToken = registrationTokenService.createToken(user.getId());
            UserInvitedEvent event = new UserInvitedEvent(
                    String.valueOf(user.getId()),
                    user.getEmail().value(),
                    user.getName(),
                    registrationToken
            );
            kafkaTemplate.send(topicUserInvited, user.getId().toString(), event);
            log.info("Sent UserInvitedEvent for user id={}, email={}", user.getId(), user.getEmail().value());
        }
        catch (Exception e) {
            log.warn("Failed to send UserInvitedEvent for user id={}, email will not be sent: {}",
                     user.getId(), e.getMessage());
        }
    }
}
