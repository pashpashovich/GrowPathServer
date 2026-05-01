package by.bsuir.growpathserver.trainee.application.listener;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import by.bsuir.growpathserver.common.model.kafka.UserInvitedEvent;
import by.bsuir.growpathserver.trainee.application.service.RegistrationTokenService;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.events.UserCreatedEvent;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    @Value("${kafka.topic.user-invited:USERINVITED}")
    private String topicUserInvited;

    private final UserRepository userRepository;
    private final RegistrationTokenService registrationTokenService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Handling UserCreatedEvent for user id={}, email={}", event.userId(), event.email());

        try {
            UserEntity userEntity = userRepository.findById(event.userId())
                    .orElseThrow(() -> new IllegalStateException("User not found with id: " + event.userId()));

            userEntity.setInvitationSentAt(LocalDateTime.now());
            userRepository.save(userEntity);

            String registrationToken = registrationTokenService.createToken(event.userId());

            UserInvitedEvent invitedEvent = new UserInvitedEvent(
                    String.valueOf(event.userId()),
                    event.email(),
                    event.firstName(),
                    event.lastName(),
                    event.patronymicName(),
                    registrationToken
            );

            kafkaTemplate.send(topicUserInvited, String.valueOf(event.userId()), invitedEvent);
            log.info("Sent UserInvitedEvent for user id={}, email={}", event.userId(), event.email());
        }
        catch (Exception e) {
            log.error("Failed to send invitation for user id={}: {}", event.userId(), e.getMessage(), e);
        }
    }
}
