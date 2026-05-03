package by.bsuir.growpathserver.trainee.application.query;

import java.time.LocalDateTime;

public record GetMentorsStatsQuery(
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        Long departmentId
) {
}
