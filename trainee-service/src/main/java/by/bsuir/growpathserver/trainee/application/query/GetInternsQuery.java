package by.bsuir.growpathserver.trainee.application.query;

import lombok.Builder;

@Builder
public record GetInternsQuery(
        Integer page,
        Integer limit,
        String search,
        String department,
        String status,
        Double rating
) {
}
