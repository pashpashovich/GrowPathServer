package by.bsuir.growpathserver.trainee.application.command;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import lombok.Builder;

@Builder
public record CreateTaskCommand(
        String title,
        String description,
        TaskPriority priority,
        Long assigneeId,
        Long mentorId,
        Long internshipId,
        Long stageId,
        LocalDateTime dueDate
) {
}
