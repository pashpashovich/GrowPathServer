package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.TasksStatsResponse;
import by.bsuir.growpathserver.trainee.application.query.GetTasksStatsQuery;
import by.bsuir.growpathserver.trainee.application.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetTasksStatsHandler {

    private final DashboardStatsService dashboardStatsService;

    public TasksStatsResponse handle(GetTasksStatsQuery query) {
        return dashboardStatsService.getTasksStats(query);
    }
}
