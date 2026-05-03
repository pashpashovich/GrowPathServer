package by.bsuir.growpathserver.trainee.application.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.dto.model.InternsStatsResponse;
import by.bsuir.growpathserver.dto.model.InternsStatsResponseDistributionByProgramInner;
import by.bsuir.growpathserver.dto.model.InternsStatsResponseFilters;
import by.bsuir.growpathserver.dto.model.InternsStatsResponseTopPerformersInner;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponse;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponseFilters;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponseTopMentorsInner;
import by.bsuir.growpathserver.dto.model.MentorsStatsResponseWorkloadDistributionInner;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponse;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponseFilters;
import by.bsuir.growpathserver.dto.model.ProgramsStatsResponseProgramStatsInner;
import by.bsuir.growpathserver.dto.model.TasksStatsResponse;
import by.bsuir.growpathserver.dto.model.TasksStatsResponseCompletionTrendInner;
import by.bsuir.growpathserver.dto.model.TasksStatsResponseFilters;
import by.bsuir.growpathserver.dto.model.TrendsResponse;
import by.bsuir.growpathserver.dto.model.TrendsResponseSummary;
import by.bsuir.growpathserver.dto.model.UpcomingDeadlinesResponse;
import by.bsuir.growpathserver.dto.model.UpcomingDeadlinesResponseDeadlinesInner;
import by.bsuir.growpathserver.dto.model.UpcomingDeadlinesResponseFilters;
import by.bsuir.growpathserver.trainee.application.query.GetInternsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetProgramsStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTasksStatsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTrendsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUpcomingDeadlinesQuery;
import by.bsuir.growpathserver.trainee.application.service.DashboardStatsService;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
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
        response.setInProgressTasks((int) tasks.stream().filter(t -> TaskStatus.IN_PROGRESS.equals(t.getStatus())).count());
        response.setSubmittedTasks((int) tasks.stream().filter(t -> TaskStatus.SUBMITTED.equals(t.getStatus())).count());
        response.setOnReviewTasks((int) tasks.stream().filter(t -> TaskStatus.ON_REVIEW.equals(t.getStatus())).count());
        response.setCompletedTasks((int) tasks.stream().filter(t -> TaskStatus.COMPLETED.equals(t.getStatus())).count());
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

        return response;
    }

    @Override
    public MentorsStatsResponse getMentorsStats(GetMentorsStatsQuery query) {
        List<UserEntity> allMentors = userRepository.findByRole(
                by.bsuir.growpathserver.trainee.domain.valueobject.UserRole.MENTOR
        );

        List<UserEntity> filteredMentors = allMentors;

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
        List<by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity> allPrograms =
                internshipProgramRepository.findAll();

        List<by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity> filteredPrograms = allPrograms;
        if (query.status() != null) {
            // TODO: Filter by status when status field is added to InternshipProgramEntity
        }

        int totalPrograms = filteredPrograms.size();
        int activePrograms = totalPrograms; // TODO: Filter by active status
        int completedPrograms = 0; // TODO: Filter by completed status
        int archivedPrograms = 0; // TODO: Filter by archived status

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
        response.setAverageCompletionRate(0.0); // TODO: Calculate when intern-program relationship is available

        // Program stats
        List<ProgramsStatsResponseProgramStatsInner> programStats = filteredPrograms.stream()
                .map(program -> {
                    ProgramsStatsResponseProgramStatsInner stat = new ProgramsStatsResponseProgramStatsInner();
                    stat.setProgramId(program.getId());
                    stat.setProgramName(program.getTitle());
                    stat.setTotalInterns(0); // TODO: Count interns in this program
                    stat.setActiveInterns(0);
                    stat.setCompletedInterns(0);
                    stat.setAverageRating(0.0);
                    stat.setCompletionRate(0.0);
                    stat.setStatus(ProgramsStatsResponseProgramStatsInner.StatusEnum.ACTIVE);
                    return stat;
                })
                .toList();
        response.setProgramStats(programStats);

        response.setMostPopularPrograms(new ArrayList<>());
        response.setBestPerformingPrograms(new ArrayList<>());

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
        // TODO: Add filtering by department and program when these relationships are available
        return true;
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
        // TODO: Implement when intern-program relationship is available
        return new ArrayList<>();
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
