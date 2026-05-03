package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.UpcomingDeadlinesResponse;
import by.bsuir.growpathserver.trainee.application.query.GetUpcomingDeadlinesQuery;
import by.bsuir.growpathserver.trainee.application.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetUpcomingDeadlinesHandler {

    private final DashboardStatsService dashboardStatsService;

    public UpcomingDeadlinesResponse handle(GetUpcomingDeadlinesQuery query) {
        return dashboardStatsService.getUpcomingDeadlines(query);
    }
}
