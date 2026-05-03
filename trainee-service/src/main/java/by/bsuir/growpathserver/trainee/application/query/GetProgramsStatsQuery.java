package by.bsuir.growpathserver.trainee.application.query;

import java.time.LocalDateTime;

public record GetProgramsStatsQuery(
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        String status
) {
}
