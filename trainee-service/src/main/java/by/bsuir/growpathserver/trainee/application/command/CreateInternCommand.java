package by.bsuir.growpathserver.trainee.application.command;

import lombok.Builder;

@Builder
public record CreateInternCommand(
        Long userId,
        String department,
        String position,
        Long internshipProgramId,
        Long mentorId
) {
}
