package by.bsuir.growpathserver.trainee.application.handler;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.common.model.kafka.PasswordResetRequestedEvent;
import by.bsuir.growpathserver.trainee.application.command.ForgetPasswordCommand;
import by.bsuir.growpathserver.trainee.application.service.PasswordResetTokenService;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForgetPasswordHandler {

    @Value("${kafka.topic.password-reset-requested:PASSWORD_RESET_REQUESTED}")
    private String topicPasswordResetRequested;

    private final UserRepository userRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void handle(ForgetPasswordCommand command) {
        String email = StringUtils.trimToNull(command.email());
        if (StringUtils.isNotBlank(email)) {
            Optional<UserEntity> userOpt = userRepository.findByEmailIgnoreCase(email);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                String resetToken = passwordResetTokenService.createToken(user.getId());
                sendPasswordResetEvent(user, resetToken);
            }
            else {
                log.info("Forgot password: no user for email (accepted silently)");
            }
        }
    }

    private void sendPasswordResetEvent(UserEntity user, String resetToken) {
        try {
            PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
                    String.valueOf(user.getId()),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPatronymicName(),
                    resetToken
            );
            kafkaTemplate.send(topicPasswordResetRequested, user.getId().toString(), event);
            log.info("Sent PasswordResetRequestedEvent for user id={}, email={}", user.getId(), user.getEmail());
        }
        catch (Exception e) {
            log.warn("Failed to send PasswordResetRequestedEvent for user id={}: {}", user.getId(), e.getMessage());
        }
    }
}
