package by.bsuir.growpathserver.trainee.application.query;

import java.time.LocalDateTime;

public record GetTrendsQuery(
        String metric,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        String groupBy
) {
}
