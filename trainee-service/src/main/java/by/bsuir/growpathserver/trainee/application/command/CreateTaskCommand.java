package by.bsuir.growpathserver.trainee.application.command;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import lombok.Builder;

@Builder
public record CreateTaskCommand(
        String title,
        String description,
        TaskPriority priority,
        String assigneeId,
        String mentorId,
        String internshipId,
        String stageId,
        LocalDateTime dueDate
) {
}
