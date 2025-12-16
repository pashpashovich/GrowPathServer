package by.bsuir.growpathserver.trainee.application.query;

import lombok.Builder;

@Builder
public record GetInternTasksQuery(
        String internId,
        String status
) {
}
