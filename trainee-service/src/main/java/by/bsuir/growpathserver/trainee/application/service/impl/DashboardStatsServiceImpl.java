package by.bsuir.growpathserver.trainee.application.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.dto.model.ChartBoxplotItem;
import by.bsuir.growpathserver.dto.model.ChartDataPoint;
import by.bsuir.growpathserver.dto.model.DashboardChartsResponse;
import by.bsuir.growpathserver.dto.model.DashboardChartsResponseFilters;
import by.bsuir.growpathserver.dto.model.InternsStatsResponse;
import by.bsuir.growpathserver.dto.model.InternsStatsResponseDistributionByProgramInner;
import by.bsuir.growpathserver.dto.model.InternsStatsResponseFilters;
import by.bsuir.growpathserver.dto.model.InternsStatsResponseTopPerformersInner;
import by.bsuir.growpathserver.dto.model.MentorWorkloadChartItem;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponse;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponseFilters;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponseTopMentorsInner;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponseWorkloadDistributionInner;
import by.bsuir.growpathserver.dto.model.ProgramCompletionChartItem;
import by.bsuir.growpathserver.dto.model.ProgramInternCountChartItem;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponse;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponseBestPerformingProgramsInner;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponseFilters;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponseMostPopularProgramsInner;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponseProgramStatsInner;
import by.bsuir.growpathserver.dto.model.TasksStatsResponse;
import by.bsuir.growpathserver.dto.model.TasksStatsResponseCompletionTrendInner;
import by.bsuir.growpathserver.dto.model.TasksStatsResponseFilters;
import by.bsuir.growpathserver.dto.model.TrendsResponse;
import by.bsuir.growpathserver.dto.model.TrendsResponseSummary;
import by.bsuir.growpathserver.dto.model.UpcomingDeadlinesResponse;
import by.bsuir.growpathserver.dto.model.UpcomingDeadlinesResponseDeadlinesInner;
import by.bsuir.growpathserver.dto.model.UpcomingDeadlinesResponseFilters;
import by.bsuir.growpathserver.trainee.application.query.GetDashboardChartsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetInternsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetProgramsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTasksStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTrendsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUpcomingDeadlinesQuery;
import by.bsuir.growpathserver.trainee.application.service.BoxplotStatistics;
import by.bsuir.growpathserver.trainee.application.service.DashboardStatsService;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.HiringDecisionType;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.ProgramParticipantRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternHiringDecisionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramParticipantRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardStatsServiceImpl implements DashboardStatsService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final InternshipProgramRepository internshipProgramRepository;
    private final InternshipProgramParticipantRepository internshipProgramParticipantRepository;
    private final IprRepository iprRepository;
    private final AssessmentRepository assessmentRepository;
    private final InternHiringDecisionRepository internHiringDecisionRepository;

    @Override
    public InternsStatsResponse getInternsStats(GetInternsStatsQuery query) {
        List<UserEntity> allInterns = userRepository.findByRole(UserRole.INTERN);

        List<UserEntity> filteredInterns = allInterns.stream()
                .filter(intern -> applyInternFilters(intern, query))
                .toList();

        int totalInterns = filteredInterns.size();
        int activeInterns = (int) filteredInterns.stream()
                .filter(user -> UserStatus.ACTIVE.equals(user.getStatus()))
                .count();

        double averageProgress = calculateAverageProgress(filteredInterns);
        double averageRating = calculateAverageRating(filteredInterns);

        InternsStatsResponse response = new InternsStatsResponse();

        InternsStatsResponseFilters filters = new InternsStatsResponseFilters();
        filters.setDateFrom(query.dateFrom());
        filters.setDateTo(query.dateTo());
        filters.setDepartmentId(query.departmentId());
        filters.setProgramId(query.programId());
        response.setFilters(filters);

        response.setTotalInterns(totalInterns);
        response.setActiveInterns(activeInterns);
        response.setCompletedInterns(0); // TODO: Implement based on internship completion status
        response.setOnHoldInterns(0); // TODO: Implement based on internship status
        response.setAverageProgress(averageProgress);
        response.setAverageRating(averageRating);

        Map<String, Integer> distributionByStatus = new HashMap<>();
        distributionByStatus.put("active", activeInterns);
        distributionByStatus.put("completed", 0);
        distributionByStatus.put("on_hold", 0);
        response.setDistributionByStatus(distributionByStatus);

        List<InternsStatsResponseDistributionByProgramInner> distributionByProgram =
                calculateDistributionByProgram(filteredInterns);
        response.setDistributionByProgram(distributionByProgram);

        List<InternsStatsResponseTopPerformersInner> topPerformers = getTopPerformers(filteredInterns, 5);
        response.setTopPerformers(topPerformers);

        List<InternsStatsResponseTopPerformersInner> needsAttention = getNeedsAttention(filteredInterns, 5);
        response.setNeedsAttention(needsAttention);

        return response;
    }

    @Override
    public TasksStatsResponse getTasksStats(GetTasksStatsQuery query) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateFrom = query.dateFrom() != null ? query.dateFrom() : now.minusMonths(1);
        LocalDateTime dateTo = query.dateTo() != null ? query.dateTo() : now;

        List<TaskEntity> tasks = taskRepository.findAll().stream()
                .filter(task -> isTaskInDateRange(task, dateFrom, dateTo))
                .filter(task -> query.status() == null || task.getStatus() == query.status())
                .filter(task -> query.priority() == null || task.getPriority() == query.priority())
                .filter(task -> query.mentorId() == null || Objects.equals(task.getMentorId(), query.mentorId()))
                .toList();

        TasksStatsResponse response = new TasksStatsResponse();

        TasksStatsResponseFilters filters = new TasksStatsResponseFilters();
        filters.setDateFrom(dateFrom);
        filters.setDateTo(dateTo);
        if (query.status() != null) {
            filters.setStatus(TasksStatsResponseFilters.StatusEnum.fromValue(query.status().name().toLowerCase()));
        }
        if (query.priority() != null) {
            filters.setPriority(
                    TasksStatsResponseFilters.PriorityEnum.fromValue(query.priority().name().toLowerCase()));
        }
        filters.setMentorId(query.mentorId());
        response.setFilters(filters);

        response.setTotalTasks(tasks.size());
        response.setPendingTasks((int) tasks.stream().filter(t -> TaskStatus.PENDING.equals(t.getStatus())).count());
        response.setInProgressTasks(
                (int) tasks.stream().filter(t -> TaskStatus.IN_PROGRESS.equals(t.getStatus())).count());
        response.setSubmittedTasks(
                (int) tasks.stream().filter(t -> TaskStatus.SUBMITTED.equals(t.getStatus())).count());
        response.setOnReviewTasks((int) tasks.stream().filter(t -> TaskStatus.ON_REVIEW.equals(t.getStatus())).count());
        response.setCompletedTasks(
                (int) tasks.stream().filter(t -> TaskStatus.COMPLETED.equals(t.getStatus())).count());
        response.setCancelledTasks((int) tasks.stream().filter(t -> TaskStatus.REJECTED.equals(t.getStatus())).count());

        int overdueTasks = (int) tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now))
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.REJECTED)
                .count();
        response.setOverdueTasks(overdueTasks);

        double avgCompletionTime = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .filter(t -> t.getCreatedAt() != null && t.getCompletedAt() != null)
                .mapToDouble(t -> ChronoUnit.DAYS.between(t.getCreatedAt(), t.getCompletedAt()))
                .average()
                .orElse(0.0);
        response.setAverageCompletionTime(avgCompletionTime);

        long completedOnTime = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .filter(t -> t.getDueDate() != null && t.getCompletedAt() != null)
                .filter(t -> t.getCompletedAt().isBefore(t.getDueDate()) || t.getCompletedAt().isEqual(t.getDueDate()))
                .count();
        long totalCompleted = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        double onTimeRate = totalCompleted > 0 ? (completedOnTime * 100.0 / totalCompleted) : 0.0;
        response.setOnTimeCompletionRate(onTimeRate);

        double avgRating = tasks.stream()
                .filter(t -> t.getRating() != null)
                .mapToInt(TaskEntity::getRating)
                .average()
                .orElse(0.0);
        response.setAverageRating(avgRating);

        Map<String, Integer> distributionByPriority = new HashMap<>();
        Arrays.stream(TaskPriority.values())
                .forEach(priority -> {
                    int count = (int) tasks.stream().filter(t -> priority.equals(t.getPriority())).count();
                    distributionByPriority.put(priority.name().toLowerCase(), count);
                });
        response.setDistributionByPriority(distributionByPriority);

        Map<String, Integer> distributionByStatus = new HashMap<>();
        Arrays.stream(TaskStatus.values())
                .forEach(status -> {
                    int count = (int) tasks.stream().filter(t -> t.getStatus() == status).count();
                    distributionByStatus.put(status.name().toLowerCase(), count);
                });
        response.setDistributionByStatus(distributionByStatus);

        String trendGroupBy = resolveGroupBy(null, dateFrom, dateTo);
        response.setCompletionTrend(calculateTrendDataPoints("completed_tasks", dateFrom, dateTo, trendGroupBy));

        return response;
    }

    @Override
    public MentorsStatsResponse getMentorsStats(GetMentorsStatsQuery query) {
        List<UserEntity> allMentors = userRepository.findByRole(UserRole.MENTOR);

        List<UserEntity> filteredMentors = allMentors.stream()
                .filter(mentor -> query.departmentId() == null
                        || Objects.equals(mentor.getDepartmentId(), query.departmentId()))
                .toList();

        int totalMentors = filteredMentors.size();
        int activeMentors = (int) filteredMentors.stream()
                .filter(m -> UserStatus.ACTIVE.equals(m.getStatus()))
                .count();

        List<UserEntity> allInterns = userRepository.findByRole(
                UserRole.INTERN
        );

        double averageInternsPerMentor = activeMentors > 0 ? (double) allInterns.size() / activeMentors : 0.0;

        // Calculate average task rating for tasks reviewed by mentors
        LocalDateTime dateFrom = query.dateFrom() != null ? query.dateFrom() : LocalDateTime.now().minusMonths(1);
        LocalDateTime dateTo = query.dateTo() != null ? query.dateTo() : LocalDateTime.now();

        List<TaskEntity> allTasks = taskRepository.findByCreatedAtBetween(dateFrom, dateTo);
        double averageTaskRating = allTasks.stream()
                .filter(t -> t.getRating() != null)
                .mapToInt(TaskEntity::getRating)
                .average()
                .orElse(0.0);

        // Calculate average review time
        double averageReviewTime = allTasks.stream()
                .filter(t -> t.getSubmittedAt() != null && t.getReviewedAt() != null)
                .mapToDouble(t -> ChronoUnit.HOURS.between(t.getSubmittedAt(), t.getReviewedAt()))
                .average()
                .orElse(0.0);

        MentorsStatsResponse response = new MentorsStatsResponse();

        MentorsStatsResponseFilters filters = new MentorsStatsResponseFilters();
        filters.setDateFrom(query.dateFrom());
        filters.setDateTo(query.dateTo());
        filters.setDepartmentId(query.departmentId());
        response.setFilters(filters);

        response.setTotalMentors(totalMentors);
        response.setActiveMentors(activeMentors);
        response.setAverageInternsPerMentor(averageInternsPerMentor);
        response.setAverageTaskRating(averageTaskRating);
        response.setAverageReviewTime(averageReviewTime);

        // Top mentors
        List<MentorsStatsResponseTopMentorsInner> topMentors = getTopMentors(filteredMentors, allInterns, 5);
        response.setTopMentors(topMentors);

        // Workload distribution
        List<MentorsStatsResponseWorkloadDistributionInner> workloadDistribution =
                calculateWorkloadDistribution(filteredMentors, allInterns);
        response.setWorkloadDistribution(workloadDistribution);

        return response;
    }

    @Override
    public ProgramsStatsResponse getProgramsStats(GetProgramsStatsQuery query) {
        List<InternshipProgramEntity> allPrograms = internshipProgramRepository.findAll();

        List<InternshipProgramEntity> filteredPrograms = allPrograms.stream()
                .filter(program -> query.status() == null
                        || program.getStatus() != null
                        && query.status().equalsIgnoreCase(program.getStatus().getValue()))
                .toList();

        int totalPrograms = filteredPrograms.size();
        int activePrograms = (int) filteredPrograms.stream()
                .filter(p -> InternshipProgramStatus.ACTIVE.equals(p.getStatus()))
                .count();
        int completedPrograms = (int) filteredPrograms.stream()
                .filter(p -> InternshipProgramStatus.COMPLETED.equals(p.getStatus()))
                .count();
        int archivedPrograms = (int) filteredPrograms.stream()
                .filter(p -> InternshipProgramStatus.ARCHIVED.equals(p.getStatus()))
                .count();

        ProgramsStatsResponse response = new ProgramsStatsResponse();

        ProgramsStatsResponseFilters filters = new ProgramsStatsResponseFilters();
        filters.setDateFrom(query.dateFrom());
        filters.setDateTo(query.dateTo());
        if (query.status() != null) {
            filters.setStatus(ProgramsStatsResponseFilters.StatusEnum.fromValue(query.status()));
        }
        response.setFilters(filters);

        response.setTotalPrograms(totalPrograms);
        response.setActivePrograms(activePrograms);
        response.setCompletedPrograms(completedPrograms);
        response.setArchivedPrograms(archivedPrograms);
        response.setAverageCompletionRate(calculateAverageProgramCompletionRate(filteredPrograms));

        List<ProgramsStatsResponseProgramStatsInner> programStats = filteredPrograms.stream()
                .map(this::toProgramStatsInner)
                .toList();
        response.setProgramStats(programStats);

        response.setMostPopularPrograms(programStats.stream()
                                                .sorted(Comparator.comparing(
                                                                ProgramsStatsResponseProgramStatsInner::getTotalInterns)
                                                                .reversed())
                                                .limit(5)
                                                .map(stat -> {
                                                    ProgramsStatsResponseMostPopularProgramsInner item =
                                                            new ProgramsStatsResponseMostPopularProgramsInner();
                                                    item.setProgramId(stat.getProgramId());
                                                    item.setProgramName(stat.getProgramName());
                                                    item.setTotalInterns(stat.getTotalInterns());
                                                    return item;
                                                })
                                                .toList());

        response.setBestPerformingPrograms(programStats.stream()
                                                   .filter(stat -> stat.getTotalInterns() != null
                                                           && stat.getTotalInterns() > 0)
                                                   .sorted(Comparator.comparing(
                                                                   ProgramsStatsResponseProgramStatsInner::getCompletionRate)
                                                                   .reversed()
                                                                   .thenComparing(
                                                                           ProgramsStatsResponseProgramStatsInner::getAverageRating,
                                                                           Comparator.reverseOrder()))
                                                   .limit(5)
                                                   .map(stat -> {
                                                       ProgramsStatsResponseBestPerformingProgramsInner item =
                                                               new ProgramsStatsResponseBestPerformingProgramsInner();
                                                       item.setProgramId(stat.getProgramId());
                                                       item.setProgramName(stat.getProgramName());
                                                       item.setCompletionRate(stat.getCompletionRate());
                                                       item.setAverageRating(stat.getAverageRating());
                                                       return item;
                                                   })
                                                   .toList());

        return response;
    }

    @Override
    public DashboardChartsResponse getDashboardCharts(GetDashboardChartsQuery query) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateFrom = query.dateFrom() != null ? query.dateFrom() : now.minusMonths(1);
        LocalDateTime dateTo = query.dateTo() != null ? query.dateTo() : now;
        String groupBy = resolveGroupBy(query.groupBy(), dateFrom, dateTo);

        Set<Long> scopedInternIds = resolveScopedInternIds(query.departmentId(), query.programId());
        List<UserEntity> scopedInterns = filterInternsByScope(scopedInternIds);
        List<UserEntity> scopedMentors = filterMentorsByScope(query.departmentId());
        List<TaskEntity> scopedTasks = filterTasksForScope(
                taskRepository.findAll(), dateFrom, dateTo, scopedInternIds, query.programId());

        DashboardChartsResponse response = new DashboardChartsResponse();
        DashboardChartsResponseFilters filters = new DashboardChartsResponseFilters();
        filters.setDateFrom(dateFrom);
        filters.setDateTo(dateTo);
        filters.setDepartmentId(query.departmentId());
        filters.setProgramId(query.programId());
        filters.setGroupBy(DashboardChartsResponseFilters.GroupByEnum.fromValue(groupBy));
        response.setFilters(filters);
        response.setGroupBy(DashboardChartsResponse.GroupByEnum.fromValue(groupBy));

        response.setTasksCompletedTrend(new ArrayList<>(toChartDataPoints(
                calculateTrendDataPoints("completed_tasks", dateFrom, dateTo, groupBy, scopedTasks, scopedInternIds))));
        response.setTasksCreatedTrend(new ArrayList<>(toChartDataPoints(
                calculateTrendDataPoints("tasks_created", dateFrom, dateTo, groupBy, scopedTasks, scopedInternIds))));
        response.setAverageTaskRatingTrend(new ArrayList<>(toChartDataPoints(
                calculateTrendDataPoints("average_rating", dateFrom, dateTo, groupBy, scopedTasks, scopedInternIds))));
        response.setAssessmentsCountTrend(new ArrayList<>(toChartDataPoints(
                calculateAssessmentsTrend(dateFrom, dateTo, groupBy, scopedInternIds, query.programId()))));
        response.setOnTimeCompletionRateTrend(new ArrayList<>(toChartDataPoints(
                calculateTrendDataPoints("task_completion_rate", dateFrom, dateTo, groupBy, scopedTasks,
                                         scopedInternIds))));

        response.setTasksByStatus(buildTaskStatusDistribution(scopedTasks));
        response.setTasksByPriority(buildTaskPriorityDistribution(scopedTasks));
        response.setInternsByProgram(new ArrayList<>(buildInternsByProgram(scopedInterns, query.programId())));
        response.setMentorsWorkload(new ArrayList<>(buildMentorsWorkloadChart(scopedMentors)));
        response.setHiringDecisionsByType(
                buildHiringDecisionsChart(dateFrom, dateTo, scopedInternIds, query.programId()));
        response.setProgramCompletionRates(
                new ArrayList<>(buildProgramCompletionChart(query.programId(), query.departmentId())));
        response.setInternProgressBuckets(buildInternProgressBuckets(scopedInterns));
        response.setBoxplots(new ArrayList<>(buildBoxplots(
                scopedTasks, scopedInterns, scopedMentors, dateFrom, dateTo, scopedInternIds, query.programId())));

        return response;
    }

    @Override
    public TrendsResponse getTrends(GetTrendsQuery query) {
        TrendsResponse response = new TrendsResponse();

        response.setMetric(TrendsResponse.MetricEnum.fromValue(query.metric()));
        response.setGroupBy(TrendsResponse.GroupByEnum.fromValue(query.groupBy()));
        response.setDateFrom(query.dateFrom());
        response.setDateTo(query.dateTo());

        // Calculate data points based on metric and groupBy
        List<TasksStatsResponseCompletionTrendInner> dataPoints = calculateTrendDataPoints(
                query.metric(),
                query.dateFrom(),
                query.dateTo(),
                query.groupBy()
        );
        response.setDataPoints(dataPoints);

        // Calculate summary
        if (!dataPoints.isEmpty()) {
            TrendsResponseSummary summary = new TrendsResponseSummary();

            double total = dataPoints.stream().mapToDouble(TasksStatsResponseCompletionTrendInner::getValue).sum();
            double average = dataPoints.stream().mapToDouble(TasksStatsResponseCompletionTrendInner::getValue).average()
                    .orElse(0.0);
            double min = dataPoints.stream().mapToDouble(TasksStatsResponseCompletionTrendInner::getValue).min()
                    .orElse(0.0);
            double max = dataPoints.stream().mapToDouble(TasksStatsResponseCompletionTrendInner::getValue).max()
                    .orElse(0.0);

            summary.setTotal(total);
            summary.setAverage(average);
            summary.setMin(min);
            summary.setMax(max);

            // Calculate trend direction
            if (dataPoints.size() >= 2) {
                double firstValue = dataPoints.get(0).getValue();
                double lastValue = dataPoints.get(dataPoints.size() - 1).getValue();
                double changePercentage = firstValue != 0 ? ((lastValue - firstValue) / firstValue) * 100 : 0;

                summary.setChangePercentage(changePercentage);

                if (changePercentage > 5) {
                    summary.setTrend(TrendsResponseSummary.TrendEnum.INCREASING);
                }
                else if (changePercentage < -5) {
                    summary.setTrend(TrendsResponseSummary.TrendEnum.DECREASING);
                }
                else {
                    summary.setTrend(TrendsResponseSummary.TrendEnum.STABLE);
                }
            }
            else {
                summary.setTrend(TrendsResponseSummary.TrendEnum.STABLE);
                summary.setChangePercentage(0.0);
            }

            response.setSummary(summary);
        }

        return response;
    }

    @Override
    public UpcomingDeadlinesResponse getUpcomingDeadlines(GetUpcomingDeadlinesQuery query) {
        LocalDateTime now = LocalDateTime.now();
        int days = query.days() != null ? query.days() : 7;
        LocalDateTime endDate = now.plusDays(days);

        List<TaskEntity> tasks = taskRepository.findAll().stream()
                .filter(t -> t.getDueDate() != null)
                .filter(t -> t.getDueDate().isAfter(now) && t.getDueDate().isBefore(endDate))
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.REJECTED)
                .filter(t -> query.mentorId() == null || Objects.equals(t.getMentorId(), query.mentorId()))
                .filter(t -> query.internId() == null || Objects.equals(t.getAssigneeId(), query.internId()))
                .filter(t -> query.priority() == null || t.getPriority() == query.priority())
                .sorted(Comparator.comparing(TaskEntity::getDueDate))
                .toList();

        UpcomingDeadlinesResponse response = new UpcomingDeadlinesResponse();

        // Set filters
        UpcomingDeadlinesResponseFilters filters = new UpcomingDeadlinesResponseFilters();
        filters.setDays(days);
        filters.setMentorId(query.mentorId());
        filters.setInternId(query.internId());
        if (query.priority() != null) {
            filters.setPriority(
                    UpcomingDeadlinesResponseFilters.PriorityEnum.fromValue(query.priority().name().toLowerCase()));
        }
        response.setFilters(filters);

        response.setTotalDeadlines(tasks.size());

        int criticalDeadlines = (int) tasks.stream()
                .filter(t -> t.getPriority() == by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority.HIGH)
                .count();
        response.setCriticalDeadlines(criticalDeadlines);

        // Overdue tasks
        int overdueTasks = (int) taskRepository.findAll().stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now))
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.REJECTED)
                .count();
        response.setOverdueTasks(overdueTasks);

        // Convert to UpcomingDeadline DTOs
        List<UpcomingDeadlinesResponseDeadlinesInner> deadlines = tasks.stream()
                .map(this::mapToUpcomingDeadline)
                .toList();
        response.setDeadlines(deadlines);

        return response;
    }

    // Helper methods

    private boolean applyInternFilters(UserEntity intern, GetInternsStatsQuery query) {
        if (Objects.nonNull(query.departmentId())
                && !Objects.equals(intern.getDepartmentId(), query.departmentId())) {
            return false;
        }
        if (Objects.nonNull(query.programId()) && !isInternOnProgram(intern.getId(), query.programId())) {
            return false;
        }
        return true;
    }

    private boolean isInternOnProgram(Long internId, Long programId) {
        return iprRepository.existsByProgram_IdAndIntern_Id(programId, internId)
                || internshipProgramParticipantRepository.existsByProgramIdAndUserIdAndRole(
                programId, internId, ProgramParticipantRole.INTERN);
    }

    private String resolveGroupBy(String requested, LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (Objects.nonNull(requested) && !requested.isBlank()) {
            return requested.toLowerCase();
        }
        long days = ChronoUnit.DAYS.between(dateFrom.toLocalDate(), dateTo.toLocalDate());
        if (days > 90) {
            return "month";
        }
        if (days > 21) {
            return "week";
        }
        return "day";
    }

    private Set<Long> resolveScopedInternIds(Long departmentId, Long programId) {
        if (Objects.isNull(departmentId) && Objects.isNull(programId)) {
            return null;
        }
        return userRepository.findByRole(UserRole.INTERN).stream()
                .filter(intern -> Objects.isNull(departmentId)
                        || Objects.equals(intern.getDepartmentId(), departmentId))
                .filter(intern -> Objects.isNull(programId) || isInternOnProgram(intern.getId(), programId))
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
    }

    private List<UserEntity> filterInternsByScope(Set<Long> scopedInternIds) {
        List<UserEntity> interns = userRepository.findByRole(UserRole.INTERN);
        if (Objects.isNull(scopedInternIds)) {
            return interns;
        }
        return interns.stream()
                .filter(intern -> scopedInternIds.contains(intern.getId()))
                .toList();
    }

    private List<UserEntity> filterMentorsByScope(Long departmentId) {
        return userRepository.findByRole(UserRole.MENTOR).stream()
                .filter(mentor -> Objects.isNull(departmentId)
                        || Objects.equals(mentor.getDepartmentId(), departmentId))
                .toList();
    }

    private List<TaskEntity> filterTasksForScope(
            List<TaskEntity> tasks,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Set<Long> scopedInternIds,
            Long programId) {
        return tasks.stream()
                .filter(task -> isTaskInDateRange(task, dateFrom, dateTo))
                .filter(task -> Objects.isNull(programId) || Objects.equals(task.getInternshipId(), programId))
                .filter(task -> Objects.isNull(scopedInternIds)
                        || (Objects.nonNull(task.getAssigneeId()) && scopedInternIds.contains(task.getAssigneeId())))
                .toList();
    }

    private List<ChartDataPoint> toChartDataPoints(List<TasksStatsResponseCompletionTrendInner> points) {
        List<ChartDataPoint> chartPoints = new ArrayList<>(points.size());
        for (TasksStatsResponseCompletionTrendInner point : points) {
            ChartDataPoint chartPoint = new ChartDataPoint();
            chartPoint.setDate(point.getDate());
            chartPoint.setLabel(point.getLabel());
            chartPoint.setValue(point.getValue());
            chartPoints.add(chartPoint);
        }
        return chartPoints;
    }

    private List<TasksStatsResponseCompletionTrendInner> calculateAssessmentsTrend(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String groupBy,
            Set<Long> scopedInternIds,
            Long programId) {
        List<AssessmentEntity> assessments = assessmentRepository.findByUpdatedAtBetween(dateFrom, dateTo).stream()
                .filter(assessment -> Objects.isNull(programId)
                        || Objects.equals(assessment.getInternshipId(), programId))
                .filter(assessment -> Objects.isNull(scopedInternIds)
                        || scopedInternIds.contains(assessment.getInternId()))
                .toList();

        List<LocalDateTime> periods = generateTimePeriods(dateFrom, dateTo, groupBy);
        List<TasksStatsResponseCompletionTrendInner> dataPoints = new ArrayList<>();
        for (int i = 0; i < periods.size() - 1; i++) {
            LocalDateTime periodStart = periods.get(i);
            LocalDateTime periodEnd = periods.get(i + 1);
            TasksStatsResponseCompletionTrendInner point = new TasksStatsResponseCompletionTrendInner();
            point.setDate(periodStart);
            point.setLabel(formatPeriodLabel(periodStart, groupBy));
            long count = assessments.stream()
                    .filter(assessment -> Objects.nonNull(assessment.getUpdatedAt()))
                    .filter(assessment -> !assessment.getUpdatedAt().isBefore(periodStart)
                            && assessment.getUpdatedAt().isBefore(periodEnd))
                    .count();
            point.setValue((double) count);
            dataPoints.add(point);
        }
        return dataPoints;
    }

    private List<TasksStatsResponseCompletionTrendInner> calculateTrendDataPoints(
            String metric,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String groupBy,
            List<TaskEntity> scopedTasks,
            Set<Long> scopedInternIds) {
        List<LocalDateTime> periods = generateTimePeriods(dateFrom, dateTo, groupBy);
        List<TasksStatsResponseCompletionTrendInner> dataPoints = new ArrayList<>();
        for (int i = 0; i < periods.size() - 1; i++) {
            LocalDateTime periodStart = periods.get(i);
            LocalDateTime periodEnd = periods.get(i + 1);
            TasksStatsResponseCompletionTrendInner point = new TasksStatsResponseCompletionTrendInner();
            point.setDate(periodStart);
            point.setLabel(formatPeriodLabel(periodStart, groupBy));
            point.setValue(calculateScopedMetricValue(
                    metric, periodStart, periodEnd, scopedTasks, scopedInternIds));
            dataPoints.add(point);
        }
        return dataPoints;
    }

    private double calculateScopedMetricValue(
            String metric,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<TaskEntity> scopedTasks,
            Set<Long> scopedInternIds) {
        return switch (metric.toLowerCase()) {
            case "tasks_created" -> scopedTasks.stream()
                    .filter(task -> Objects.nonNull(task.getCreatedAt()))
                    .filter(task -> !task.getCreatedAt().isBefore(periodStart)
                            && task.getCreatedAt().isBefore(periodEnd))
                    .count();
            case "completed_tasks" -> scopedTasks.stream()
                    .filter(task -> TaskStatus.COMPLETED.equals(task.getStatus()))
                    .filter(task -> Objects.nonNull(task.getCompletedAt()))
                    .filter(task -> !task.getCompletedAt().isBefore(periodStart)
                            && task.getCompletedAt().isBefore(periodEnd))
                    .count();
            case "average_rating" -> scopedTasks.stream()
                    .filter(task -> Objects.nonNull(task.getRating()))
                    .filter(task -> Objects.nonNull(task.getCompletedAt()))
                    .filter(task -> !task.getCompletedAt().isBefore(periodStart)
                            && task.getCompletedAt().isBefore(periodEnd))
                    .mapToInt(TaskEntity::getRating)
                    .average()
                    .orElse(0.0);
            case "task_completion_rate" -> {
                List<TaskEntity> completedInPeriod = scopedTasks.stream()
                        .filter(task -> TaskStatus.COMPLETED.equals(task.getStatus()))
                        .filter(task -> Objects.nonNull(task.getCompletedAt()))
                        .filter(task -> !task.getCompletedAt().isBefore(periodStart)
                                && task.getCompletedAt().isBefore(periodEnd))
                        .toList();
                if (completedInPeriod.isEmpty()) {
                    yield 0.0;
                }
                long onTime = completedInPeriod.stream()
                        .filter(task -> Objects.nonNull(task.getDueDate()))
                        .filter(task -> !task.getCompletedAt().isAfter(task.getDueDate()))
                        .count();
                yield onTime * 100.0 / completedInPeriod.size();
            }
            case "new_interns" -> userRepository.findByRole(UserRole.INTERN).stream()
                    .filter(intern -> Objects.isNull(scopedInternIds) || scopedInternIds.contains(intern.getId()))
                    .filter(intern -> Objects.nonNull(intern.getCreatedAt()))
                    .filter(intern -> !intern.getCreatedAt().isBefore(periodStart)
                            && intern.getCreatedAt().isBefore(periodEnd))
                    .count();
            case "active_users" -> scopedTasks.stream()
                    .filter(task -> Objects.nonNull(task.getCreatedAt()))
                    .filter(task -> !task.getCreatedAt().isBefore(periodStart)
                            && task.getCreatedAt().isBefore(periodEnd))
                    .map(TaskEntity::getAssigneeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            default -> 0.0;
        };
    }

    private Map<String, Integer> buildTaskStatusDistribution(List<TaskEntity> tasks) {
        Map<String, Integer> distribution = new HashMap<>();
        Arrays.stream(TaskStatus.values()).forEach(status -> distribution.put(
                status.name().toLowerCase(),
                (int) tasks.stream().filter(task -> task.getStatus() == status).count()));
        return distribution;
    }

    private Map<String, Integer> buildTaskPriorityDistribution(List<TaskEntity> tasks) {
        Map<String, Integer> distribution = new HashMap<>();
        Arrays.stream(TaskPriority.values()).forEach(priority -> distribution.put(
                priority.name().toLowerCase(),
                (int) tasks.stream().filter(task -> priority.equals(task.getPriority())).count()));
        return distribution;
    }

    private List<ProgramInternCountChartItem> buildInternsByProgram(
            List<UserEntity> scopedInterns,
            Long programIdFilter) {
        Map<Long, Long> internCountByProgram = new HashMap<>();
        Map<Long, String> programNames = new HashMap<>();

        for (UserEntity intern : scopedInterns) {
            Long programId = resolveInternProgramId(intern.getId());
            if (Objects.isNull(programId)) {
                continue;
            }
            if (Objects.nonNull(programIdFilter) && !Objects.equals(programId, programIdFilter)) {
                continue;
            }
            internCountByProgram.merge(programId, 1L, Long::sum);
            programNames.putIfAbsent(programId, resolveProgramTitle(programId));
        }

        return internCountByProgram.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(entry -> {
                    ProgramInternCountChartItem item = new ProgramInternCountChartItem();
                    item.setProgramId(entry.getKey());
                    item.setProgramName(programNames.getOrDefault(entry.getKey(), "Program " + entry.getKey()));
                    item.setCount(entry.getValue().intValue());
                    return item;
                })
                .toList();
    }

    private Long resolveInternProgramId(Long internId) {
        return iprRepository.findActiveByInternId(internId)
                .map(ipr -> ipr.getProgram().getId())
                .or(() -> iprRepository.findByInternId(internId).stream()
                        .max(Comparator.comparing(
                                by.bsuir.growpathserver.trainee.domain.entity.IprEntity::getUpdatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(ipr -> ipr.getProgram().getId()))
                .or(() -> internshipProgramParticipantRepository
                        .findByUserIdAndRole(internId, ProgramParticipantRole.INTERN).stream()
                        .findFirst()
                        .map(participant -> participant.getProgram().getId()))
                .orElse(null);
    }

    private String resolveProgramTitle(Long programId) {
        return internshipProgramRepository.findById(programId)
                .map(InternshipProgramEntity::getTitle)
                .orElse("Program " + programId);
    }

    private List<MentorWorkloadChartItem> buildMentorsWorkloadChart(List<UserEntity> mentors) {
        return mentors.stream()
                .map(mentor -> {
                    List<TaskEntity> mentorTasks = taskRepository.findByMentorId(mentor.getId());
                    MentorWorkloadChartItem item = new MentorWorkloadChartItem();
                    item.setMentorId(mentor.getId());
                    item.setMentorName(mentor.getFirstName() + " " + mentor.getLastName());
                    long activeInterns = mentorTasks.stream()
                            .map(TaskEntity::getAssigneeId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();
                    item.setActiveInterns((int) activeInterns);
                    int activeTasks = (int) mentorTasks.stream()
                            .filter(task -> task.getStatus() != TaskStatus.COMPLETED
                                    && task.getStatus() != TaskStatus.REJECTED)
                            .count();
                    item.setActiveTasks(activeTasks);
                    item.setWorkloadLevel(resolveWorkloadLevel((int) activeInterns));
                    return item;
                })
                .sorted(Comparator.comparing(MentorWorkloadChartItem::getActiveTasks).reversed())
                .toList();
    }

    private MentorWorkloadChartItem.WorkloadLevelEnum resolveWorkloadLevel(int activeInterns) {
        if (activeInterns == 0) {
            return MentorWorkloadChartItem.WorkloadLevelEnum.LOW;
        }
        if (activeInterns <= 3) {
            return MentorWorkloadChartItem.WorkloadLevelEnum.NORMAL;
        }
        if (activeInterns <= 5) {
            return MentorWorkloadChartItem.WorkloadLevelEnum.HIGH;
        }
        return MentorWorkloadChartItem.WorkloadLevelEnum.OVERLOADED;
    }

    private Map<String, Integer> buildHiringDecisionsChart(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Set<Long> scopedInternIds,
            Long programId) {
        Map<String, Integer> distribution = new HashMap<>();
        for (HiringDecisionType type : HiringDecisionType.values()) {
            distribution.put(type.toApiValue(), 0);
        }
        internHiringDecisionRepository.findByDecidedAtBetween(dateFrom, dateTo).stream()
                .filter(decision -> Objects.isNull(programId)
                        || Objects.equals(decision.getProgram().getId(), programId))
                .filter(decision -> Objects.isNull(scopedInternIds)
                        || scopedInternIds.contains(decision.getIntern().getId()))
                .forEach(decision -> distribution.merge(decision.getDecision().toApiValue(), 1, Integer::sum));
        return distribution;
    }

    private List<ProgramCompletionChartItem> buildProgramCompletionChart(Long programId, Long departmentId) {
        List<InternshipProgramEntity> programs = internshipProgramRepository.findAll().stream()
                .filter(program -> Objects.isNull(programId) || Objects.equals(program.getId(), programId))
                .toList();

        return programs.stream()
                .map(program -> {
                    Set<Long> internIds = resolveProgramInternIds(program.getId(), departmentId);
                    ProgramCompletionChartItem item = new ProgramCompletionChartItem();
                    item.setProgramId(program.getId());
                    item.setProgramName(program.getTitle());
                    item.setInternCount(internIds.size());
                    item.setCompletionRate(calculateInternSetCompletionRate(internIds));
                    return item;
                })
                .filter(item -> item.getInternCount() > 0)
                .sorted(Comparator.comparing(ProgramCompletionChartItem::getCompletionRate).reversed())
                .toList();
    }

    private Set<Long> resolveProgramInternIds(Long programId, Long departmentId) {
        Set<Long> internIds = new HashSet<>();
        internshipProgramParticipantRepository
                .findByProgramIdAndRole(programId, ProgramParticipantRole.INTERN)
                .stream()
                .map(participant -> participant.getUser().getId())
                .forEach(internIds::add);
        userRepository.findByRole(UserRole.INTERN).stream()
                .filter(intern -> iprRepository.existsByProgram_IdAndIntern_Id(programId, intern.getId()))
                .map(UserEntity::getId)
                .forEach(internIds::add);
        if (Objects.nonNull(departmentId)) {
            return internIds.stream()
                    .map(userRepository::findById)
                    .flatMap(java.util.Optional::stream)
                    .filter(intern -> Objects.equals(intern.getDepartmentId(), departmentId))
                    .map(UserEntity::getId)
                    .collect(Collectors.toSet());
        }
        return internIds;
    }

    private List<ChartBoxplotItem> buildBoxplots(
            List<TaskEntity> scopedTasks,
            List<UserEntity> scopedInterns,
            List<UserEntity> scopedMentors,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Set<Long> scopedInternIds,
            Long programIdFilter) {
        List<ChartBoxplotItem> boxplots = new ArrayList<>();
        Set<Long> programIds = resolveBoxplotProgramIds(scopedTasks, scopedInterns, programIdFilter);

        for (Long programId : programIds) {
            String programLabel = resolveProgramTitle(programId);
            String groupKey = "program:" + programId;

            boxplots.add(toBoxplotItem(
                    ChartBoxplotItem.MetricEnum.TASK_RATING,
                    groupKey,
                    programLabel,
                    collectTaskRatings(scopedTasks, programId, dateFrom, dateTo)));
            boxplots.add(toBoxplotItem(
                    ChartBoxplotItem.MetricEnum.TASK_COMPLETION_DAYS,
                    groupKey,
                    programLabel,
                    collectTaskCompletionDays(scopedTasks, programId, dateFrom, dateTo)));
            boxplots.add(toBoxplotItem(
                    ChartBoxplotItem.MetricEnum.ASSESSMENT_OVERALL_RATING,
                    groupKey,
                    programLabel,
                    collectAssessmentRatings(dateFrom, dateTo, programId, scopedInternIds)));
            boxplots.add(toBoxplotItem(
                    ChartBoxplotItem.MetricEnum.INTERN_PROGRESS_PERCENT,
                    groupKey,
                    programLabel,
                    collectInternProgressPercents(scopedInterns, programId)));
        }

        for (UserEntity mentor : scopedMentors) {
            String mentorLabel = mentor.getFirstName() + " " + mentor.getLastName();
            boxplots.add(toBoxplotItem(
                    ChartBoxplotItem.MetricEnum.TASK_RATING,
                    "mentor:" + mentor.getId(),
                    mentorLabel,
                    collectTaskRatingsForMentor(scopedTasks, mentor.getId(), dateFrom, dateTo)));
        }

        boxplots.add(toBoxplotItem(
                ChartBoxplotItem.MetricEnum.TASK_RATING,
                "all",
                "Все программы",
                collectTaskRatings(scopedTasks, null, dateFrom, dateTo)));
        boxplots.add(toBoxplotItem(
                ChartBoxplotItem.MetricEnum.TASK_COMPLETION_DAYS,
                "all",
                "Все программы",
                collectTaskCompletionDays(scopedTasks, null, dateFrom, dateTo)));
        boxplots.add(toBoxplotItem(
                ChartBoxplotItem.MetricEnum.ASSESSMENT_OVERALL_RATING,
                "all",
                "Все программы",
                collectAssessmentRatings(dateFrom, dateTo, programIdFilter, scopedInternIds)));
        boxplots.add(toBoxplotItem(
                ChartBoxplotItem.MetricEnum.INTERN_PROGRESS_PERCENT,
                "all",
                "Все стажёры",
                collectInternProgressPercents(scopedInterns, null)));

        return boxplots.stream()
                .filter(item -> item.getSampleSize() > 0)
                .toList();
    }

    private Set<Long> resolveBoxplotProgramIds(
            List<TaskEntity> scopedTasks,
            List<UserEntity> scopedInterns,
            Long programIdFilter) {
        if (Objects.nonNull(programIdFilter)) {
            return Set.of(programIdFilter);
        }
        Set<Long> programIds = new HashSet<>();
        scopedTasks.stream()
                .map(TaskEntity::getInternshipId)
                .filter(Objects::nonNull)
                .forEach(programIds::add);
        scopedInterns.stream()
                .map(UserEntity::getId)
                .map(this::resolveInternProgramId)
                .filter(Objects::nonNull)
                .forEach(programIds::add);
        return programIds;
    }

    private List<Double> collectTaskRatings(
            List<TaskEntity> tasks,
            Long programId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo) {
        return tasks.stream()
                .filter(task -> TaskStatus.COMPLETED.equals(task.getStatus()))
                .filter(task -> Objects.isNull(programId) || Objects.equals(task.getInternshipId(), programId))
                .filter(task -> Objects.nonNull(task.getRating()))
                .filter(task -> isCompletedInPeriod(task, dateFrom, dateTo))
                .map(task -> task.getRating().doubleValue())
                .toList();
    }

    private List<Double> collectTaskRatingsForMentor(
            List<TaskEntity> tasks,
            Long mentorId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo) {
        return tasks.stream()
                .filter(task -> Objects.equals(task.getMentorId(), mentorId))
                .filter(task -> TaskStatus.COMPLETED.equals(task.getStatus()))
                .filter(task -> Objects.nonNull(task.getRating()))
                .filter(task -> isCompletedInPeriod(task, dateFrom, dateTo))
                .map(task -> task.getRating().doubleValue())
                .toList();
    }

    private List<Double> collectTaskCompletionDays(
            List<TaskEntity> tasks,
            Long programId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo) {
        return tasks.stream()
                .filter(task -> TaskStatus.COMPLETED.equals(task.getStatus()))
                .filter(task -> Objects.isNull(programId) || Objects.equals(task.getInternshipId(), programId))
                .filter(task -> Objects.nonNull(task.getCreatedAt()) && Objects.nonNull(task.getCompletedAt()))
                .filter(task -> isCompletedInPeriod(task, dateFrom, dateTo))
                .mapToDouble(task -> (double) ChronoUnit.DAYS.between(task.getCreatedAt(), task.getCompletedAt()))
                .filter(days -> days >= 0)
                .boxed()
                .toList();
    }

    private List<Double> collectAssessmentRatings(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Long programId,
            Set<Long> scopedInternIds) {
        return assessmentRepository.findByUpdatedAtBetween(dateFrom, dateTo).stream()
                .filter(assessment -> Objects.nonNull(assessment.getOverallRating()))
                .filter(assessment -> Objects.isNull(programId)
                        || Objects.equals(assessment.getInternshipId(), programId))
                .filter(assessment -> Objects.isNull(scopedInternIds)
                        || scopedInternIds.contains(assessment.getInternId()))
                .map(AssessmentEntity::getOverallRating)
                .toList();
    }

    private List<Double> collectInternProgressPercents(List<UserEntity> interns, Long programId) {
        return interns.stream()
                .filter(intern -> Objects.isNull(programId) || isInternOnProgram(intern.getId(), programId))
                .map(intern -> calculateInternProgressPercent(intern.getId()))
                .toList();
    }

    private boolean isCompletedInPeriod(TaskEntity task, LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (Objects.isNull(task.getCompletedAt())) {
            return false;
        }
        return !task.getCompletedAt().isBefore(dateFrom) && !task.getCompletedAt().isAfter(dateTo);
    }

    private ChartBoxplotItem toBoxplotItem(
            ChartBoxplotItem.MetricEnum metric,
            String groupKey,
            String groupLabel,
            List<Double> values) {
        BoxplotStatistics stats = BoxplotStatistics.fromValues(values);
        ChartBoxplotItem item = new ChartBoxplotItem();
        item.setMetric(metric);
        item.setGroupKey(groupKey);
        item.setGroupLabel(groupLabel);
        item.setMin(stats.getMin());
        item.setQ1(stats.getQ1());
        item.setMedian(stats.getMedian());
        item.setQ3(stats.getQ3());
        item.setMax(stats.getMax());
        item.setOutliers(new ArrayList<>(stats.getOutliers()));
        item.setSampleSize(stats.getSampleSize());
        return item;
    }

    private Map<String, Integer> buildInternProgressBuckets(List<UserEntity> interns) {
        Map<String, Integer> buckets = new LinkedHashMap<>();
        buckets.put("0-25", 0);
        buckets.put("26-50", 0);
        buckets.put("51-75", 0);
        buckets.put("76-100", 0);
        for (UserEntity intern : interns) {
            String bucket = resolveProgressBucket(calculateInternProgressPercent(intern.getId()));
            buckets.merge(bucket, 1, Integer::sum);
        }
        return buckets;
    }

    private double calculateInternProgressPercent(Long internId) {
        List<TaskEntity> tasks = taskRepository.findByAssigneeId(internId);
        if (tasks.isEmpty()) {
            return 0.0;
        }
        long completed = tasks.stream().filter(task -> TaskStatus.COMPLETED.equals(task.getStatus())).count();
        return completed * 100.0 / tasks.size();
    }

    private String resolveProgressBucket(double progress) {
        if (progress <= 25.0) {
            return "0-25";
        }
        if (progress <= 50.0) {
            return "26-50";
        }
        if (progress <= 75.0) {
            return "51-75";
        }
        return "76-100";
    }

    private double calculateInternSetCompletionRate(Set<Long> internIds) {
        if (internIds.isEmpty()) {
            return 0.0;
        }
        return internIds.stream()
                .mapToDouble(this::calculateInternProgressPercent)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageProgramCompletionRate(List<InternshipProgramEntity> programs) {
        if (programs.isEmpty()) {
            return 0.0;
        }
        return programs.stream()
                .mapToDouble(
                        program -> calculateInternSetCompletionRate(resolveProgramInternIds(program.getId(), null)))
                .average()
                .orElse(0.0);
    }

    private ProgramsStatsResponseProgramStatsInner toProgramStatsInner(InternshipProgramEntity program) {
        Set<Long> internIds = resolveProgramInternIds(program.getId(), null);
        ProgramsStatsResponseProgramStatsInner stat = new ProgramsStatsResponseProgramStatsInner();
        stat.setProgramId(program.getId());
        stat.setProgramName(program.getTitle());
        stat.setTotalInterns(internIds.size());
        stat.setActiveInterns((int) internIds.stream()
                .map(userRepository::findById)
                .flatMap(java.util.Optional::stream)
                .filter(user -> UserStatus.ACTIVE.equals(user.getStatus()))
                .count());
        stat.setCompletedInterns(stat.getTotalInterns() - stat.getActiveInterns());
        stat.setCompletionRate(calculateInternSetCompletionRate(internIds));
        stat.setAverageRating(calculateInternSetAverageRating(internIds));
        stat.setStatus(mapProgramStatus(program.getStatus()));
        return stat;
    }

    private double calculateInternSetAverageRating(Set<Long> internIds) {
        return internIds.stream()
                .map(taskRepository::getAverageRatingByAssigneeId)
                .filter(Objects::nonNull)
                .filter(rating -> rating > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private ProgramsStatsResponseProgramStatsInner.StatusEnum mapProgramStatus(InternshipProgramStatus status) {
        if (Objects.isNull(status)) {
            return ProgramsStatsResponseProgramStatsInner.StatusEnum.ACTIVE;
        }
        return switch (status) {
            case COMPLETED -> ProgramsStatsResponseProgramStatsInner.StatusEnum.COMPLETED;
            case ARCHIVED -> ProgramsStatsResponseProgramStatsInner.StatusEnum.ARCHIVED;
            default -> ProgramsStatsResponseProgramStatsInner.StatusEnum.ACTIVE;
        };
    }

    private boolean isTaskInDateRange(TaskEntity task, LocalDateTime from, LocalDateTime to) {
        if (task.getCreatedAt() == null) {
            return false;
        }
        return !task.getCreatedAt().isBefore(from) && !task.getCreatedAt().isAfter(to);
    }

    private double calculateAverageProgress(List<UserEntity> interns) {
        if (interns.isEmpty()) {
            return 0.0;
        }

        double totalProgress = 0.0;
        int count = 0;

        for (UserEntity intern : interns) {
            List<TaskEntity> tasks = taskRepository.findByAssigneeId(intern.getId());
            if (!tasks.isEmpty()) {
                long completedTasks = tasks.stream()
                        .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                        .count();
                double progress = (completedTasks * 100.0) / tasks.size();
                totalProgress += progress;
                count++;
            }
        }

        return count > 0 ? totalProgress / count : 0.0;
    }

    private double calculateAverageRating(List<UserEntity> interns) {
        if (interns.isEmpty()) {
            return 0.0;
        }

        double totalRating = 0.0;
        int count = 0;

        for (UserEntity intern : interns) {
            Double avgRating = taskRepository.getAverageRatingByAssigneeId(intern.getId());
            if (avgRating != null && avgRating > 0) {
                totalRating += avgRating;
                count++;
            }
        }

        return count > 0 ? totalRating / count : 0.0;
    }

    private List<InternsStatsResponseDistributionByProgramInner> calculateDistributionByProgram(List<UserEntity> interns) {
        Map<Long, Long> counts = new HashMap<>();
        Map<Long, String> names = new HashMap<>();
        for (UserEntity intern : interns) {
            Long programId = resolveInternProgramId(intern.getId());
            if (Objects.isNull(programId)) {
                continue;
            }
            counts.merge(programId, 1L, Long::sum);
            names.putIfAbsent(programId, resolveProgramTitle(programId));
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(entry -> {
                    InternsStatsResponseDistributionByProgramInner item =
                            new InternsStatsResponseDistributionByProgramInner();
                    item.setProgramId(entry.getKey());
                    item.setProgramName(names.getOrDefault(entry.getKey(), "Program " + entry.getKey()));
                    item.setCount(entry.getValue().intValue());
                    return item;
                })
                .toList();
    }

    private List<InternsStatsResponseTopPerformersInner> getTopPerformers(List<UserEntity> interns, int limit) {
        return interns.stream()
                .map(intern -> {
                    InternsStatsResponseTopPerformersInner performer = new InternsStatsResponseTopPerformersInner();
                    performer.setInternId(intern.getId());
                    performer.setFirstName(intern.getFirstName());
                    performer.setLastName(intern.getLastName());
                    performer.setAvatarUrl(intern.getAvatarUrl());

                    Long completedTasks = taskRepository.countCompletedTasksByAssigneeId(intern.getId());
                    performer.setCompletedTasks(completedTasks != null ? completedTasks.intValue() : 0);

                    Double avgRating = taskRepository.getAverageRatingByAssigneeId(intern.getId());
                    performer.setAverageRating(avgRating != null ? avgRating : 0.0);

                    // Calculate progress
                    List<TaskEntity> tasks = taskRepository.findByAssigneeId(intern.getId());
                    if (!tasks.isEmpty()) {
                        long completed = tasks.stream()
                                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                                .count();
                        performer.setProgress((completed * 100.0) / tasks.size());
                    }
                    else {
                        performer.setProgress(0.0);
                    }

                    return performer;
                })
                .filter(p -> p.getAverageRating() != null && p.getAverageRating() > 0)
                .sorted(Comparator.comparing(InternsStatsResponseTopPerformersInner::getAverageRating).reversed()
                                .thenComparing(InternsStatsResponseTopPerformersInner::getCompletedTasks,
                                               Comparator.reverseOrder()))
                .limit(limit)
                .toList();
    }

    private List<InternsStatsResponseTopPerformersInner> getNeedsAttention(List<UserEntity> interns, int limit) {
        return interns.stream()
                .map(intern -> {
                    InternsStatsResponseTopPerformersInner performer = new InternsStatsResponseTopPerformersInner();
                    performer.setInternId(intern.getId());
                    performer.setFirstName(intern.getFirstName());
                    performer.setLastName(intern.getLastName());
                    performer.setAvatarUrl(intern.getAvatarUrl());

                    Long completedTasks = taskRepository.countCompletedTasksByAssigneeId(intern.getId());
                    performer.setCompletedTasks(completedTasks != null ? completedTasks.intValue() : 0);

                    Double avgRating = taskRepository.getAverageRatingByAssigneeId(intern.getId());
                    performer.setAverageRating(avgRating != null ? avgRating : 0.0);

                    // Calculate progress
                    List<TaskEntity> tasks = taskRepository.findByAssigneeId(intern.getId());
                    if (!tasks.isEmpty()) {
                        long completed = tasks.stream()
                                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                                .count();
                        performer.setProgress((completed * 100.0) / tasks.size());
                    }
                    else {
                        performer.setProgress(0.0);
                    }

                    return performer;
                })
                .sorted(Comparator.comparing(InternsStatsResponseTopPerformersInner::getProgress)
                                .thenComparing(InternsStatsResponseTopPerformersInner::getAverageRating))
                .limit(limit)
                .toList();
    }

    private List<TasksStatsResponseCompletionTrendInner> calculateTrendDataPoints(
            String metric, LocalDateTime dateFrom, LocalDateTime dateTo, String groupBy) {
        List<TasksStatsResponseCompletionTrendInner> dataPoints = new ArrayList<>();

        // Generate time periods based on groupBy
        List<LocalDateTime> periods = generateTimePeriods(dateFrom, dateTo, groupBy);

        for (int i = 0; i < periods.size() - 1; i++) {
            LocalDateTime periodStart = periods.get(i);
            LocalDateTime periodEnd = periods.get(i + 1);

            TasksStatsResponseCompletionTrendInner point = new TasksStatsResponseCompletionTrendInner();
            point.setDate(periodStart);
            point.setLabel(formatPeriodLabel(periodStart, groupBy));

            // Calculate value based on metric
            double value = calculateMetricValue(metric, periodStart, periodEnd);
            point.setValue(value);

            dataPoints.add(point);
        }

        return dataPoints;
    }

    private List<LocalDateTime> generateTimePeriods(LocalDateTime start, LocalDateTime end, String groupBy) {
        List<LocalDateTime> periods = new ArrayList<>();
        LocalDateTime current = start;

        while (current.isBefore(end) || current.isEqual(end)) {
            periods.add(current);

            switch (groupBy.toLowerCase()) {
                case "day":
                    current = current.plusDays(1);
                    break;
                case "week":
                    current = current.plusWeeks(1);
                    break;
                case "month":
                    current = current.plusMonths(1);
                    break;
                default:
                    current = current.plusDays(1);
            }
        }

        // Add end date if not already included
        if (!periods.contains(end)) {
            periods.add(end);
        }

        return periods;
    }

    private String formatPeriodLabel(LocalDateTime date, String groupBy) {
        switch (groupBy.toLowerCase()) {
            case "day":
                return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"));
            case "week":
                return "Week " + date.format(java.time.format.DateTimeFormatter.ofPattern("w"));
            case "month":
                return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"));
            default:
                return date.toString();
        }
    }

    private double calculateMetricValue(String metric, LocalDateTime periodStart, LocalDateTime periodEnd) {
        switch (metric.toLowerCase()) {
            case "new_interns":
                return userRepository.findByRole(by.bsuir.growpathserver.trainee.domain.valueobject.UserRole.INTERN)
                        .stream()
                        .filter(u -> u.getCreatedAt() != null)
                        .filter(u -> !u.getCreatedAt().isBefore(periodStart) && u.getCreatedAt().isBefore(periodEnd))
                        .count();

            case "completed_tasks":
                return taskRepository.findByCreatedAtBetween(periodStart, periodEnd)
                        .stream()
                        .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                        .filter(t -> t.getCompletedAt() != null)
                        .filter(t -> !t.getCompletedAt().isBefore(periodStart) && t.getCompletedAt()
                                .isBefore(periodEnd))
                        .count();

            case "average_rating":
                return taskRepository.findByCreatedAtBetween(periodStart, periodEnd)
                        .stream()
                        .filter(t -> t.getRating() != null)
                        .mapToInt(TaskEntity::getRating)
                        .average()
                        .orElse(0.0);

            case "task_completion_rate":
                List<TaskEntity> periodTasks = taskRepository.findByCreatedAtBetween(periodStart, periodEnd);
                if (periodTasks.isEmpty()) {
                    return 0.0;
                }
                long completedOnTime = periodTasks.stream()
                        .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                        .filter(t -> t.getDueDate() != null && t.getCompletedAt() != null)
                        .filter(t -> t.getCompletedAt().isBefore(t.getDueDate()) || t.getCompletedAt()
                                .isEqual(t.getDueDate()))
                        .count();
                long totalCompleted = periodTasks.stream()
                        .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                        .count();
                return totalCompleted > 0 ? (completedOnTime * 100.0 / totalCompleted) : 0.0;

            case "active_users":
                // Count users who have activity in this period (created tasks, completed tasks, etc.)
                return taskRepository.findByCreatedAtBetween(periodStart, periodEnd)
                        .stream()
                        .map(TaskEntity::getAssigneeId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();

            default:
                return 0.0;
        }
    }

    private List<MentorsStatsResponseTopMentorsInner> getTopMentors(
            List<UserEntity> mentors, List<UserEntity> allInterns, int limit) {
        return mentors.stream()
                .map(mentor -> {
                    MentorsStatsResponseTopMentorsInner topMentor = new MentorsStatsResponseTopMentorsInner();
                    topMentor.setMentorId(mentor.getId());
                    topMentor.setFirstName(mentor.getFirstName());
                    topMentor.setLastName(mentor.getLastName());
                    topMentor.setAvatarUrl(mentor.getAvatarUrl());

                    // Count active interns for this mentor
                    List<TaskEntity> mentorTasks = taskRepository.findByMentorId(mentor.getId());
                    long activeInterns = mentorTasks.stream()
                            .map(TaskEntity::getAssigneeId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();
                    topMentor.setActiveInterns((int) activeInterns);

                    // Count completed tasks
                    long completedTasks = mentorTasks.stream()
                            .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                            .count();
                    topMentor.setCompletedTasks((int) completedTasks);

                    // Calculate average rating
                    double avgRating = mentorTasks.stream()
                            .filter(t -> t.getRating() != null)
                            .mapToInt(TaskEntity::getRating)
                            .average()
                            .orElse(0.0);
                    topMentor.setAverageRating(avgRating);

                    return topMentor;
                })
                .filter(m -> m.getCompletedTasks() > 0)
                .sorted(Comparator.comparing(MentorsStatsResponseTopMentorsInner::getAverageRating).reversed()
                                .thenComparing(MentorsStatsResponseTopMentorsInner::getCompletedTasks,
                                               Comparator.reverseOrder()))
                .limit(limit)
                .toList();
    }

    private List<MentorsStatsResponseWorkloadDistributionInner> calculateWorkloadDistribution(
            List<UserEntity> mentors, List<UserEntity> allInterns) {
        return mentors.stream()
                .map(mentor -> {
                    MentorsStatsResponseWorkloadDistributionInner workload =
                            new MentorsStatsResponseWorkloadDistributionInner();
                    workload.setMentorId(mentor.getId());
                    workload.setMentorName(mentor.getFirstName() + " " + mentor.getLastName());

                    List<TaskEntity> mentorTasks = taskRepository.findByMentorId(mentor.getId());

                    // Count active interns
                    long activeInterns = mentorTasks.stream()
                            .map(TaskEntity::getAssigneeId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();
                    workload.setActiveInterns((int) activeInterns);

                    // Count active tasks (not completed or rejected)
                    long activeTasks = mentorTasks.stream()
                            .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.REJECTED)
                            .count();
                    workload.setActiveTasks((int) activeTasks);

                    // Determine workload level
                    String workloadLevel;
                    if (activeInterns == 0) {
                        workloadLevel = "low";
                    }
                    else if (activeInterns <= 3) {
                        workloadLevel = "normal";
                    }
                    else if (activeInterns <= 5) {
                        workloadLevel = "high";
                    }
                    else {
                        workloadLevel = "overloaded";
                    }
                    workload.setWorkloadLevel(
                            MentorsStatsResponseWorkloadDistributionInner.WorkloadLevelEnum.fromValue(workloadLevel)
                    );

                    return workload;
                })
                .sorted(Comparator.comparing(MentorsStatsResponseWorkloadDistributionInner::getActiveInterns)
                                .reversed())
                .toList();
    }

    private UpcomingDeadlinesResponseDeadlinesInner mapToUpcomingDeadline(TaskEntity task) {
        UpcomingDeadlinesResponseDeadlinesInner deadline = new UpcomingDeadlinesResponseDeadlinesInner();
        deadline.setTaskId(task.getId());
        deadline.setTitle(task.getTitle());
        deadline.setDueDate(task.getDueDate());
        deadline.setPriority(UpcomingDeadlinesResponseDeadlinesInner.PriorityEnum.fromValue(
                task.getPriority().name().toLowerCase()));
        deadline.setStatus(
                UpcomingDeadlinesResponseDeadlinesInner.StatusEnum.fromValue(task.getStatus().name().toLowerCase()));
        deadline.setAssigneeId(task.getAssigneeId());
        deadline.setMentorId(task.getMentorId());
        return deadline;
    }
}
