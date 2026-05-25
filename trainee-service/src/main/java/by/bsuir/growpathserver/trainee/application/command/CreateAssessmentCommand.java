package by.bsuir.growpathserver.trainee.application.command;

import lombok.Builder;

@Builder
public record CreateAssessmentCommand(
        Long internId,
        Long mentorId,
        Long internshipId,
        Long iprStageId,
        Double overallRating,
        Double qualityRating,
        Double speedRating,
        Double communicationRating,
        String comment
) {
}
