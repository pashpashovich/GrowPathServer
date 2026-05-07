package by.bsuir.growpathserver.notification.listener;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.common.model.kafka.ApplicationCreatedEvent;
import by.bsuir.growpathserver.common.model.kafka.PasswordResetRequestedEvent;
import by.bsuir.growpathserver.common.model.kafka.TaskCompletedEvent;
import by.bsuir.growpathserver.common.model.kafka.UserBlockedEvent;
import by.bsuir.growpathserver.common.model.kafka.UserInvitedEvent;
import by.bsuir.growpathserver.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @Value("${app.registration-base-url:http://localhost:3000}")
    private String registrationBaseUrl;

    @Value("${app.password-reset-base-url:http://localhost:3000}")
    private String passwordResetBaseUrl;

    @KafkaListener(
            topics = "${kafka.topic.user-invited}",
            groupId = "notification-service-group",
            containerFactory = "userInvitedListenerFactory"
    )
    public void onUserInvited(UserInvitedEvent event) {
        log.info("Received UserInvitedEvent for user id={}, email={}", event.getUserId(), event.getEmail());
        if (event.getEmail() == null) {
            return;
        }
        String registrationLink = registrationBaseUrl + "/register";
        if (StringUtils.isNotEmpty(event.getRegistrationToken())) {
            registrationLink += "?token=" + event.getRegistrationToken();
        }
        String fullName = Stream.of(event.getLastName(), event.getFirstName(), event.getPatronymicName())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "))
                .trim();
        String greeting = "Здравствуйте, " + fullName + "!";
        Map<String, Object> variables = Map.of(
                "greeting", greeting,
                "email", event.getEmail(),
                "registrationLink", registrationLink
        );
        notificationService.sendEmail(
                event.getEmail(),
                "Приглашение в GrowPath",
                "user-invited",
                variables
        );
    }

    @KafkaListener(
            topics = "${kafka.topic.password-reset-requested}",
            groupId = "notification-service-group",
            containerFactory = "passwordResetRequestedListenerFactory"
    )
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        log.info("Received PasswordResetRequestedEvent for user id={}, email={}", event.getUserId(), event.getEmail());
        if (StringUtils.isNotEmpty(event.getEmail())) {
            String resetLink = passwordResetBaseUrl + "/reset-password";
            if (StringUtils.isNotEmpty(event.getResetToken())) {
                resetLink += "?token=" + event.getResetToken();
            }
            String fullName = Stream.of(event.getLastName(), event.getFirstName(), event.getPatronymicName())
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(" "))
                    .trim();
            String greeting = StringUtils.isNotBlank(fullName) ? "Здравствуйте, " + fullName + "!" : "Здравствуйте!";
            Map<String, Object> variables = Map.of(
                    "greeting", greeting,
                    "email", event.getEmail(),
                    "resetLink", resetLink
            );
            notificationService.sendEmail(
                    event.getEmail(),
                    "Восстановление пароля GrowPath",
                    "password-reset",
                    variables
            );
        }
    }

    @KafkaListener(
            topics = "${kafka.topic.application-created}",
            groupId = "notification-service-group",
            containerFactory = "applicationCreatedListenerFactory"
    )
    public void onApplicationCreated(ApplicationCreatedEvent event) {
        log.info("Received ApplicationCreatedEvent for applicationId={}, email={}", event.getApplicationId(),
                 event.getEmail());
        if (event.getEmail() == null) {
            return;
        }
        Map<String, Object> variables = Map.of(
                "applicationId", event.getApplicationId() != null ? event.getApplicationId() : ""
        );
        notificationService.sendEmail(
                event.getEmail(),
                "Новая заявка на стажировку",
                "application-created",
                variables
        );
    }

    @KafkaListener(
            topics = "${kafka.topic.task-completed}",
            groupId = "notification-service-group",
            containerFactory = "taskCompletedListenerFactory"
    )
    public void onTaskCompleted(TaskCompletedEvent event) {
        log.info("Received TaskCompletedEvent for task={}, email={}", event.getTaskName(), event.getEmail());
        if (event.getEmail() == null) {
            return;
        }
        Map<String, Object> variables = Map.of(
                "taskName", event.getTaskName() != null ? event.getTaskName() : ""
        );
        notificationService.sendEmail(
                event.getEmail(),
                "Задача выполнена",
                "task-completed",
                variables
        );
    }

    @KafkaListener(
            topics = "${kafka.topic.user-blocked}",
            groupId = "notification-service-group",
            containerFactory = "userBlockedListenerFactory"
    )
    public void onUserBlocked(UserBlockedEvent event) {
        log.info("Received UserBlockedEvent for user id={}, email={}", event.getUserId(), event.getEmail());
        if (event.getEmail() == null) {
            return;
        }
        String fullName = Stream.of(event.getLastName(), event.getFirstName(), event.getPatronymicName())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "))
                .trim();
        String greeting = StringUtils.isNotBlank(fullName) ? "Здравствуйте, " + fullName + "!" : "Здравствуйте!";
        Map<String, Object> variables = Map.of(
                "greeting", greeting,
                "email", event.getEmail()
        );
        notificationService.sendEmail(
                event.getEmail(),
                "Блокировка учетной записи GrowPath",
                "user-blocked",
                variables
        );
    }
}
