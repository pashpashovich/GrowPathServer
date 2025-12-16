package by.bsuir.growpathserver.trainee.application.command;

import lombok.Builder;

@Builder
public record CreateDepartmentCommand(
        String name,
        String description
) {
}
