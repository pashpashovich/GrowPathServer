package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.trainee.domain.events.TaskReviewResultEvent;

public interface TaskReviewNotificationService {
    void notifyReviewResult(TaskReviewResultEvent event);
}
