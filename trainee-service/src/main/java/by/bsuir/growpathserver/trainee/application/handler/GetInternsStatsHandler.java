package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.InternsStatsResponse;
import by.bsuir.growpathserver.trainee.application.query.GetInternsStatsQuery;
import by.bsuir.growpathserver.trainee.application.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternsStatsHandler {

    private final DashboardStatsService dashboardStatsService;

    public InternsStatsResponse handle(GetInternsStatsQuery query) {
        return dashboardStatsService.getInternsStats(query);
    }
}
