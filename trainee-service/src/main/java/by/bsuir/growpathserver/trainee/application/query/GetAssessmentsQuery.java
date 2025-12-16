package by.bsuir.growpathserver.trainee.application.query;

import lombok.Builder;

@Builder
public record GetAssessmentsQuery(
        Integer page,
        Integer limit,
        String internId,
        String mentorId,
        String internshipId
) {
}
