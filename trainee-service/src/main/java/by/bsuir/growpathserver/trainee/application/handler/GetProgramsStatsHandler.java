package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.ProgramsStatsResponse;
import by.bsuir.growpathserver.trainee.application.query.GetProgramsStatsQuery;
import by.bsuir.growpathserver.trainee.application.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetProgramsStatsHandler {

    private final DashboardStatsService dashboardStatsService;

    public ProgramsStatsResponse handle(GetProgramsStatsQuery query) {
        return dashboardStatsService.getProgramsStats(query);
    }
}
