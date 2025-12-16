package by.bsuir.growpathserver.trainee.application.command;

import lombok.Builder;

@Builder
public record CreateAssessmentCommand(
        String internId,
        String mentorId,
        String internshipId,
        Double overallRating,
        Double qualityRating,
        Double speedRating,
        Double communicationRating,
        String comment
) {
}
