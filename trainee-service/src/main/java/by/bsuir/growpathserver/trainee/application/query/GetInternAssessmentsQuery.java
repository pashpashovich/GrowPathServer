package by.bsuir.growpathserver.trainee.application.query;

import lombok.Builder;

@Builder
public record GetInternAssessmentsQuery(
        String internId,
        Integer page,
        Integer limit
) {
}
