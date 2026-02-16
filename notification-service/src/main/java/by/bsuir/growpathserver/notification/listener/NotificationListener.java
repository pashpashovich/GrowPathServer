package by.bsuir.growpathserver.notification.listener;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.common.model.kafka.ApplicationCreatedEvent;
import by.bsuir.growpathserver.common.model.kafka.TaskCompletedEvent;
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
        String registrationLink = registrationBaseUrl.endsWith("/") ?
                registrationBaseUrl + "register" :
                registrationBaseUrl + "/register";
        if (event.getRegistrationToken() != null && !event.getRegistrationToken().isBlank()) {
            registrationLink += "?token=" + event.getRegistrationToken();
        }
        String greeting = (event.getUserName() != null && !event.getUserName().isBlank())
                ? "Здравствуйте, " + event.getUserName() + "!"
                : "Здравствуйте!";
        Map<String, Object> variables = Map.of(
                "greeting", greeting,
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
}
