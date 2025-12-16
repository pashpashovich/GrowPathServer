package by.bsuir.growpathserver.trainee.application.command;

import lombok.Builder;

@Builder
public record UpdateInternCommand(
        String internId,
        String department,
        String position,
        String status,
        String mentorId
) {
}
