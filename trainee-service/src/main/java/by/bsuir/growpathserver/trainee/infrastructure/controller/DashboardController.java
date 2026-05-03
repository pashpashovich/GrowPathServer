package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.DashboardApi;
import by.bsuir.growpathserver.dto.model.DashboardResponse;
import by.bsuir.growpathserver.dto.model.InternsStatsResponse;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponse;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponse;
import by.bsuir.growpathserver.dto.model.TasksStatsResponse;
import by.bsuir.growpathserver.dto.model.TrendsResponse;
import by.bsuir.growpathserver.dto.model.UpcomingDeadlinesResponse;
import by.bsuir.growpathserver.trainee.application.handler.GetInternsStatsHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetMentorsStatsHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetProgramsStatsHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetTasksStatsHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetTrendsHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetUpcomingDeadlinesHandler;
import by.bsuir.growpathserver.trainee.application.query.GetInternsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetProgramsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTasksStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTrendsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUpcomingDeadlinesQuery;
import by.bsuir.growpathserver.trainee.application.service.ReportFacade;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DashboardController extends BaseController implements DashboardApi {

    private final ReportFacade reportFacade;
    private final GetInternsStatsHandler getInternsStatsHandler;
    private final GetTasksStatsHandler getTasksStatsHandler;
    private final GetMentorsStatsHandler getMentorsStatsHandler;
    private final GetProgramsStatsHandler getProgramsStatsHandler;
    private final GetTrendsHandler getTrendsHandler;
    private final GetUpcomingDeadlinesHandler getUpcomingDeadlinesHandler;

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboard(
            String role,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Long departmentId,
            Long programId,
            Long mentorId,
            String status) {
        return ResponseEntity.ok(reportFacade.getDashboard(role));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'MENTOR')")
    public ResponseEntity<InternsStatsResponse> getInternsStats(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Long departmentId,
            Long programId) {
        GetInternsStatsQuery query = new GetInternsStatsQuery(
                dateFrom,
                dateTo,
                departmentId,
                programId
        );
        return ResponseEntity.ok(getInternsStatsHandler.handle(query));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'MENTOR')")
    public ResponseEntity<TasksStatsResponse> getTasksStats(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String status,
            String priority,
            Long mentorId) {
        GetTasksStatsQuery query = new GetTasksStatsQuery(
                dateFrom,
                dateTo,
                status != null ? TaskStatus.valueOf(status.toUpperCase()) : null,
                priority != null ? TaskPriority.valueOf(priority.toUpperCase()) : null,
                mentorId
        );
        return ResponseEntity.ok(getTasksStatsHandler.handle(query));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MentorsStatsResponse> getMentorsStats(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Long departmentId) {
        GetMentorsStatsQuery query = new GetMentorsStatsQuery(
                dateFrom,
                dateTo,
                departmentId
        );
        return ResponseEntity.ok(getMentorsStatsHandler.handle(query));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<ProgramsStatsResponse> getProgramsStats(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String status) {
        GetProgramsStatsQuery query = new GetProgramsStatsQuery(
                dateFrom,
                dateTo,
                status
        );
        return ResponseEntity.ok(getProgramsStatsHandler.handle(query));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'MENTOR')")
    public ResponseEntity<TrendsResponse> getTrends(
            String metric,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String groupBy) {
        GetTrendsQuery query = new GetTrendsQuery(
                metric,
                dateFrom,
                dateTo,
                groupBy != null ? groupBy : "day"
        );
        return ResponseEntity.ok(getTrendsHandler.handle(query));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'MENTOR', 'INTERN')")
    public ResponseEntity<UpcomingDeadlinesResponse> getUpcomingDeadlines(
            Integer days,
            Long mentorId,
            Long internId,
            String priority) {
        GetUpcomingDeadlinesQuery query = new GetUpcomingDeadlinesQuery(
                days,
                mentorId,
                internId,
                priority != null ? TaskPriority.valueOf(priority.toUpperCase()) : null
        );
        return ResponseEntity.ok(getUpcomingDeadlinesHandler.handle(query));
    }
}
