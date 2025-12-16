package by.bsuir.growpathserver.trainee.application.command;

import lombok.Builder;

@Builder
public record UpdateDepartmentCommand(
        Long id,
        String name,
        String description
) {
}
