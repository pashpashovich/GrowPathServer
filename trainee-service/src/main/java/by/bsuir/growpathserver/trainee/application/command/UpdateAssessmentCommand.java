package by.bsuir.growpathserver.trainee.application.command;

import lombok.Builder;

@Builder
public record UpdateAssessmentCommand(
        Long id,
        Double overallRating,
        Double qualityRating,
        Double speedRating,
        Double communicationRating,
        String comment
) {
}
