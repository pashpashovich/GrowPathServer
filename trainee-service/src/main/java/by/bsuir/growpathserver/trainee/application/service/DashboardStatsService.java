package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.dto.model.DashboardChartsResponse;
import by.bsuir.growpathserver.dto.model.InternsStatsResponse;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponse;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponse;
import by.bsuir.growpathserver.dto.model.TasksStatsResponse;
import by.bsuir.growpathserver.dto.model.TrendsResponse;
import by.bsuir.growpathserver.dto.model.UpcomingDeadlinesResponse;
import by.bsuir.growpathserver.trainee.application.query.GetDashboardChartsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetInternsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetProgramsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTasksStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTrendsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUpcomingDeadlinesQuery;

public interface DashboardStatsService {

    InternsStatsResponse getInternsStats(GetInternsStatsQuery query);

    TasksStatsResponse getTasksStats(GetTasksStatsQuery query);

    MentorsStatsResponse getMentorsStats(GetMentorsStatsQuery query);

    ProgramsStatsResponse getProgramsStats(GetProgramsStatsQuery query);

    TrendsResponse getTrends(GetTrendsQuery query);

    UpcomingDeadlinesResponse getUpcomingDeadlines(GetUpcomingDeadlinesQuery query);

    DashboardChartsResponse getDashboardCharts(GetDashboardChartsQuery query);
}
