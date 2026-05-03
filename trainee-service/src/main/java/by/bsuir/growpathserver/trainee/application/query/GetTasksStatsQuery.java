package by.bsuir.growpathserver.trainee.application.query;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;

public record GetTasksStatsQuery(
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        TaskStatus status,
        TaskPriority priority,
        Long mentorId
) {
}
