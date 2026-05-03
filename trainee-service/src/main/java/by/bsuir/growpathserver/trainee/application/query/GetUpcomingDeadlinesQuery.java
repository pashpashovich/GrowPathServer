package by.bsuir.growpathserver.trainee.application.query;

import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;

public record GetUpcomingDeadlinesQuery(
        Integer days,
        Long mentorId,
        Long internId,
        TaskPriority priority
) {
}
