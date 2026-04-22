package by.bsuir.growpathserver.trainee.application.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.trainee.application.service.TaskReviewNotificationService;
import by.bsuir.growpathserver.trainee.domain.events.TaskReviewResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskReviewNotificationServiceImpl implements TaskReviewNotificationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.task-reviewed:TASKREVIEWED}")
    private String topicTaskReviewed;

    @Override
    @Async
    public void notifyReviewResult(TaskReviewResultEvent event) {
        kafkaTemplate.send(topicTaskReviewed, event.taskId().toString(), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to publish task review notification for taskId={}", event.taskId(), throwable);
                    }
                });
    }
}
