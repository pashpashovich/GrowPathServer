package by.bsuir.growpathserver.trainee.application.query;

import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import lombok.Builder;

@Builder
public record GetTasksQuery(
        Integer page,
        Integer limit,
        TaskStatus status,
        String assignee,
        TaskPriority priority,
        String internshipId,
        String mentorId
) {
}
