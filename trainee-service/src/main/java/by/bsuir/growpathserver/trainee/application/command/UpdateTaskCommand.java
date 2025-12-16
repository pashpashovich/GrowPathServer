package by.bsuir.growpathserver.trainee.application.command;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import lombok.Builder;

@Builder
public record UpdateTaskCommand(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        String assigneeId,
        String stageId,
        LocalDateTime dueDate
) {
}
