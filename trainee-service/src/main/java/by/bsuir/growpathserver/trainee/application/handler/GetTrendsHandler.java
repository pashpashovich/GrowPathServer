package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.TrendsResponse;
import by.bsuir.growpathserver.trainee.application.query.GetTrendsQuery;
import by.bsuir.growpathserver.trainee.application.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetTrendsHandler {

    private final DashboardStatsService dashboardStatsService;

    public TrendsResponse handle(GetTrendsQuery query) {
        return dashboardStatsService.getTrends(query);
    }
}
