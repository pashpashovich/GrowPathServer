package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.DashboardChartsResponse;
import by.bsuir.growpathserver.trainee.application.query.GetDashboardChartsQuery;
import by.bsuir.growpathserver.trainee.application.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetDashboardChartsHandler {

    private final DashboardStatsService dashboardStatsService;

    public DashboardChartsResponse handle(GetDashboardChartsQuery query) {
        return dashboardStatsService.getDashboardCharts(query);
    }
}
