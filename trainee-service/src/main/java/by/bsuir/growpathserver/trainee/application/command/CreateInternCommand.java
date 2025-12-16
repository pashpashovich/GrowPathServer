package by.bsuir.growpathserver.trainee.application.command;

import lombok.Builder;

@Builder
public record CreateInternCommand(
        String userId,
        String department,
        String position,
        String internshipProgramId,
        String mentorId
) {
}
