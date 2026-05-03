package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.MentorsStatsResponse;
import by.bsuir.growpathserver.trainee.application.query.GetMentorsStatsQuery;
import by.bsuir.growpathserver.trainee.application.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetMentorsStatsHandler {

    private final DashboardStatsService dashboardStatsService;

    public MentorsStatsResponse handle(GetMentorsStatsQuery query) {
        return dashboardStatsService.getMentorsStats(query);
    }
}
