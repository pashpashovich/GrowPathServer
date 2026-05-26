package by.bsuir.growpathserver.trainee.application.query;

import java.time.LocalDateTime;

public record GetDashboardChartsQuery(
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        Long departmentId,
        Long programId,
        String groupBy
) {
}
