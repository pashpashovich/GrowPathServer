package by.bsuir.growpathserver.trainee.application.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.DashboardResponse;
import by.bsuir.growpathserver.dto.model.DashboardResponseStats;
import by.bsuir.growpathserver.dto.model.InternStats;
import by.bsuir.growpathserver.dto.model.MentorPerformance;
import by.bsuir.growpathserver.dto.model.MentorStats;
import by.bsuir.growpathserver.dto.model.MentorWorkloadResponse;
import by.bsuir.growpathserver.dto.model.MentorWorkloadResponseDataInner;
import by.bsuir.growpathserver.dto.model.PeriodStats;
import by.bsuir.growpathserver.dto.model.PeriodStatsMonthlyInner;
import by.bsuir.growpathserver.dto.model.PeriodStatsWeeklyInner;
import by.bsuir.growpathserver.dto.model.ReportListResponse;
import by.bsuir.growpathserver.dto.model.ReportListResponseDataInner;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportFacade {

    private final TaskRepository taskRepository;
    private final AssessmentRepository assessmentRepository;
    private final InternshipProgramRepository internshipProgramRepository;
    private final UserRepository userRepository;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;

    @Transactional(readOnly = true)
    public ReportListResponse getReports(String programId,
                                         String mentorId,
                                         String period,
                                         LocalDate startDate,
                                         LocalDate endDate) {
        requireCurrentUser();
        log.info("Report query: programId='{}', mentorId='{}', period='{}', startDate={}, endDate={}",
                  programId, mentorId, period, startDate, endDate);
        List<TaskEntity> filteredTasks = filterTasks(programId, mentorId, startDate, endDate);
        Map<Long, InternshipProgramEntity> programsById = internshipProgramRepository.findAll().stream()
                .collect(Collectors.toMap(InternshipProgramEntity::getId, p -> p));
        List<AssessmentEntity> assessments = assessmentRepository.findAll();
        List<ReportListResponseDataInner> rows = filteredTasks.stream()
                .collect(Collectors.groupingBy(TaskEntity::getInternshipId))
                .entrySet()
                .stream()
                .map(entry -> buildProgramRow(entry.getKey(), entry.getValue(), assessments, programsById, period))
                .sorted(Comparator.comparing(ReportListResponseDataInner::getProgramTitle,
                                             Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        ReportListResponse response = new ReportListResponse();
        response.setData(rows);
        return response;
    }

    @Transactional(readOnly = true)
    public MentorWorkloadResponse getMentorWorkload(String mentorId) {
        requireCurrentUser();
        log.info("Mentor workload query: mentorId='{}'", mentorId);
        Long mentorFilter = parseOptionalLongQueryParam(mentorId, "mentorId");
        List<TaskEntity> tasks = taskRepository.findAll().stream()
                .filter(t -> Objects.nonNull(t.getMentorId()))
                .filter(t -> mentorFilter == null || Objects.equals(t.getMentorId(), mentorFilter))
                .toList();

        Map<Long, UserEntity> users = userRepository.findAll().stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
        Map<Long, String> programTitles = internshipProgramRepository.findAll().stream()
                .collect(Collectors.toMap(InternshipProgramEntity::getId, InternshipProgramEntity::getTitle));
        List<AssessmentEntity> assessments = assessmentRepository.findAll();

        List<MentorWorkloadResponseDataInner> data = tasks.stream()
                .collect(Collectors.groupingBy(TaskEntity::getMentorId))
                .entrySet()
                .stream()
                .map(entry -> buildMentorRow(entry.getKey(), entry.getValue(), users, assessments, programTitles))
                .sorted(Comparator.comparing(MentorWorkloadResponseDataInner::getMentorName,
                                             Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        MentorWorkloadResponse response = new MentorWorkloadResponse();
        response.setData(data);
        return response;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String role) {
        requireCurrentUser();
        DashboardResponse response = new DashboardResponse();
        DashboardResponseStats stats = new DashboardResponseStats();

        stats.setTotalUsers((int) userRepository.count());
        stats.setActiveInterns(
                (int) userRepository.findAll().stream().filter(u -> u.getRole() == UserRole.INTERN).count());
        stats.setActivePrograms((int) internshipProgramRepository.findAll().stream()
                .filter(p -> p.getStatus() == InternshipProgramStatus.ACTIVE).count());
        stats.setPendingTasks((int) taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING).count());
        stats.setCompletedTasks((int) taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());

        DashboardResponse.RoleEnum roleEnum = DashboardResponse.RoleEnum.HR;
        if (role != null && !role.isBlank()) {
            roleEnum = DashboardResponse.RoleEnum.fromValue(role);
        }
        response.setRole(roleEnum);
        response.setStats(stats);
        response.setRecentActivity(List.of());
        response.setUpcomingDeadlines(List.of());
        return response;
    }

    @Transactional(readOnly = true)
    public byte[] exportReportsCsv(String programId,
                                   String mentorId,
                                   String period,
                                   LocalDate startDate,
                                   LocalDate endDate) {
        ReportListResponse response = getReports(programId, mentorId, period, startDate, endDate);
        StringBuilder builder = new StringBuilder(
                "programId,programTitle,totalTasks,completedTasks,overdueTasks,inProgressTasks,completionRate,avgCompletionHours\n");
        for (ReportListResponseDataInner row : response.getData()) {
            builder.append(row.getProgramId()).append(',')
                    .append(csv(row.getProgramTitle())).append(',')
                    .append(zero(row.getTotalTasks())).append(',')
                    .append(zero(row.getCompletedTasks())).append(',')
                    .append(zero(row.getOverdueTasks())).append(',')
                    .append(zero(row.getInProgressTasks())).append(',')
                    .append(round(row.getCompletionRate())).append(',')
                    .append(round(row.getAverageCompletionTime()))
                    .append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportMentorWorkloadCsv(String mentorId) {
        MentorWorkloadResponse response = getMentorWorkload(mentorId);
        StringBuilder builder =
                new StringBuilder(
                        "mentorId,mentorName,email,totalInterns,activeTasks,pendingReviews,completedReviews,averageReviewHours,workload\n");
        for (MentorWorkloadResponseDataInner row : response.getData()) {
            builder.append(row.getMentorId()).append(',')
                    .append(csv(row.getMentorName())).append(',')
                    .append(csv(row.getEmail())).append(',')
                    .append(zero(row.getTotalInterns())).append(',')
                    .append(zero(row.getActiveTasks())).append(',')
                    .append(zero(row.getPendingReviews())).append(',')
                    .append(zero(row.getCompletedReviews())).append(',')
                    .append(round(row.getAverageReviewTime())).append(',')
                    .append(row.getWorkload() != null ? row.getWorkload().getValue() : "")
                    .append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ReportListResponseDataInner buildProgramRow(Long programId,
                                                        List<TaskEntity> tasks,
                                                        List<AssessmentEntity> assessments,
                                                        Map<Long, InternshipProgramEntity> programsById,
                                                        String period) {
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        int inProgress = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        int overdue = (int) tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now())
                        && t.getStatus() != TaskStatus.COMPLETED)
                .count();
        double completionRate = total == 0 ? 0.0 : (completed * 100.0) / total;
        double avgCompletionHours = tasks.stream()
                .filter(t -> t.getTakenAt() != null && t.getCompletedAt() != null)
                .mapToLong(t -> Duration.between(t.getTakenAt(), t.getCompletedAt()).toHours())
                .average()
                .orElse(0.0);

        ReportListResponseDataInner row = new ReportListResponseDataInner();
        row.setProgramId(programId);
        row.setProgramTitle(
                programsById.containsKey(programId) ? programsById.get(programId).getTitle() : "Program " + programId);
        row.setTotalTasks(total);
        row.setCompletedTasks(completed);
        row.setInProgressTasks(inProgress);
        row.setOverdueTasks(overdue);
        row.setCompletionRate(round(completionRate));
        row.setAverageCompletionTime(round(avgCompletionHours));
        row.setInternStats(buildInternStats(tasks));
        row.setMentorStats(buildMentorStats(tasks));
        row.setPeriodStats(buildPeriodStats(tasks, period));

        return row;
    }

    private List<Object> buildInternStats(List<TaskEntity> tasks) {
        Map<Long, String> internNames = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.INTERN)
                .collect(Collectors.toMap(UserEntity::getId, this::fullName));
        return tasks.stream()
                .filter(t -> t.getAssigneeId() != null)
                .collect(Collectors.groupingBy(TaskEntity::getAssigneeId))
                .entrySet()
                .stream()
                .map(entry -> {
                    int total = entry.getValue().size();
                    int completed = (int) entry.getValue().stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                            .count();
                    InternStats stats = new InternStats();
                    stats.setInternId(entry.getKey());
                    stats.setInternName(internNames.getOrDefault(entry.getKey(), "Intern " + entry.getKey()));
                    stats.setTotalTasks(total);
                    stats.setCompletedTasks(completed);
                    stats.setCompletionRate(total == 0 ? 0.0 : round((completed * 100.0) / total));
                    return (Object) stats;
                })
                .sorted((a, b) -> {
                    InternStats i1 = (InternStats) a;
                    InternStats i2 = (InternStats) b;
                    return i1.getInternName().compareToIgnoreCase(i2.getInternName());
                })
                .toList();
    }

    private List<Object> buildMentorStats(List<TaskEntity> tasks) {
        Map<Long, String> mentorNames = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.MENTOR)
                .collect(Collectors.toMap(UserEntity::getId, this::fullName));
        return tasks.stream()
                .filter(t -> t.getMentorId() != null)
                .collect(Collectors.groupingBy(TaskEntity::getMentorId))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<TaskEntity> mentorTasks = entry.getValue();
                    int active = (int) mentorTasks.stream()
                            .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS
                                    || t.getStatus() == TaskStatus.ON_REVIEW)
                            .count();
                    int completedReviews = (int) mentorTasks.stream()
                            .filter(t -> t.getStatus() == TaskStatus.COMPLETED
                                    || t.getStatus() == TaskStatus.NEEDS_REWORK)
                            .count();
                    double avgReviewHours = mentorTasks.stream()
                            .filter(t -> t.getSubmittedAt() != null && t.getReviewedAt() != null)
                            .mapToLong(t -> Duration.between(t.getSubmittedAt(), t.getReviewedAt()).toHours())
                            .average()
                            .orElse(0.0);
                    Set<Long> interns = mentorTasks.stream().map(TaskEntity::getAssigneeId).filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    MentorStats stats = new MentorStats();
                    stats.setMentorId(entry.getKey());
                    stats.setMentorName(mentorNames.getOrDefault(entry.getKey(), "Mentor " + entry.getKey()));
                    stats.setAssignedInterns(interns.size());
                    stats.setActiveTasks(active);
                    stats.setCompletedReviews(completedReviews);
                    stats.setAverageReviewTime(round(avgReviewHours));
                    stats.setWorkload(mapWorkload(active));
                    return (Object) stats;
                })
                .toList();
    }

    private MentorWorkloadResponseDataInner buildMentorRow(Long mentorId,
                                                           List<TaskEntity> tasks,
                                                           Map<Long, UserEntity> users,
                                                           List<AssessmentEntity> assessments,
                                                           Map<Long, String> programTitles) {
        UserEntity mentor = users.get(mentorId);
        int activeTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS || t.getStatus() == TaskStatus.ON_REVIEW)
                .count();
        int pendingReviews = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.ON_REVIEW).count();
        int completedReviews = (int) tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED || t.getStatus() == TaskStatus.NEEDS_REWORK)
                .count();
        double avgReviewHours = tasks.stream()
                .filter(t -> t.getSubmittedAt() != null && t.getReviewedAt() != null)
                .mapToLong(t -> Duration.between(t.getSubmittedAt(), t.getReviewedAt()).toHours())
                .average()
                .orElse(0.0);
        Set<Long> interns = tasks.stream().map(TaskEntity::getAssigneeId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<String> programs = tasks.stream()
                .map(TaskEntity::getInternshipId)
                .distinct()
                .map(id -> programTitles.getOrDefault(id, "Program " + id))
                .sorted()
                .toList();
        LocalDateTime lastActivity = tasks.stream()
                .map(TaskEntity::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        MentorPerformance performance = new MentorPerformance();
        List<AssessmentEntity> mentorAssessments = assessments.stream()
                .filter(a -> Objects.equals(a.getMentorId(), mentorId)).toList();
        performance.setResponseTime(round(avgReviewHours));
        performance.setQualityScore(round(mentorAssessments.stream()
                                                  .mapToDouble(a -> doubleOrZero(a.getQualityRating()))
                                                  .average()
                                                  .orElse(0.0)));
        performance.setInternSatisfaction(round(mentorAssessments.stream()
                                                        .mapToDouble(a -> doubleOrZero(a.getOverallRating()))
                                                        .average()
                                                        .orElse(0.0)));

        MentorWorkloadResponseDataInner row = new MentorWorkloadResponseDataInner();
        row.setMentorId(mentorId);
        row.setMentorName(mentor != null ? fullName(mentor) : "Mentor " + mentorId);
        row.setEmail(mentor != null ? mentor.getEmail() : null);
        row.setTotalInterns(interns.size());
        row.setActiveTasks(activeTasks);
        row.setPendingReviews(pendingReviews);
        row.setCompletedReviews(completedReviews);
        row.setAverageReviewTime(round(avgReviewHours));
        row.setWorkload(mapWorkloadWorkload(activeTasks));
        row.setPrograms(programs);
        row.setLastActivity(lastActivity);
        row.setPerformance(performance);
        return row;
    }

    private Object buildPeriodStats(List<TaskEntity> tasks, String period) {
        if (period == null || period.isBlank() || "program".equalsIgnoreCase(period)) {
            return null;
        }
        PeriodStats stats = new PeriodStats();
        if ("weekly".equalsIgnoreCase(period)) {
            Map<String, List<TaskEntity>> grouped = tasks.stream()
                    .collect(Collectors.groupingBy(t -> {
                        LocalDateTime created = t.getCreatedAt() != null ? t.getCreatedAt() : LocalDateTime.now();
                        int week = created.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
                        return created.getYear() + "-W" + week;
                    }, LinkedHashMap::new, Collectors.toList()));
            List<PeriodStatsWeeklyInner> weekly = new ArrayList<>();
            grouped.forEach((week, weekTasks) -> {
                PeriodStatsWeeklyInner row = new PeriodStatsWeeklyInner();
                row.setWeek(week);
                row.setCreated(weekTasks.size());
                row.setCompleted((int) weekTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());
                row.setOverdue((int) weekTasks.stream()
                        .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now())
                                && t.getStatus() != TaskStatus.COMPLETED)
                        .count());
                weekly.add(row);
            });
            stats.setWeekly(weekly);
        }
        if ("monthly".equalsIgnoreCase(period)) {
            Map<String, List<TaskEntity>> grouped = tasks.stream()
                    .collect(Collectors.groupingBy(t -> {
                        LocalDateTime created = t.getCreatedAt() != null ? t.getCreatedAt() : LocalDateTime.now();
                        return created.getYear() + "-" + String.format("%02d", created.getMonthValue());
                    }, LinkedHashMap::new, Collectors.toList()));
            List<PeriodStatsMonthlyInner> monthly = new ArrayList<>();
            grouped.forEach((month, monthTasks) -> {
                PeriodStatsMonthlyInner row = new PeriodStatsMonthlyInner();
                row.setMonth(month);
                row.setCreated(monthTasks.size());
                row.setCompleted((int) monthTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());
                row.setOverdue((int) monthTasks.stream()
                        .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now())
                                && t.getStatus() != TaskStatus.COMPLETED)
                        .count());
                monthly.add(row);
            });
            stats.setMonthly(monthly);
        }
        return stats;
    }

    private List<TaskEntity> filterTasks(String programId, String mentorId, LocalDate startDate, LocalDate endDate) {
        Long program = parseLongOrNull(programId);
        Long mentor = parseLongOrNull(mentorId);
        LocalDateTime from = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime to = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        log.info("Resolved report filters: programId={}, mentorId={}, from={}, to={}", program, mentor, from, to);

        return taskRepository.findAll().stream()
                .filter(t -> program == null || Objects.equals(t.getInternshipId(), program))
                .filter(t -> mentor == null || Objects.equals(t.getMentorId(), mentor))
                .filter(t -> from == null || (t.getCreatedAt() != null && !t.getCreatedAt().isBefore(from)))
                .filter(t -> to == null || (t.getCreatedAt() != null && t.getCreatedAt().isBefore(to)))
                .toList();
    }

    private Long requireCurrentUser() {
        return currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    private MentorStats.WorkloadEnum mapWorkload(int activeTasks) {
        if (activeTasks >= 15) {
            return MentorStats.WorkloadEnum.OVERLOAD;
        }
        if (activeTasks >= 8) {
            return MentorStats.WorkloadEnum.HIGH;
        }
        return MentorStats.WorkloadEnum.NORMAL;
    }

    private MentorWorkloadResponseDataInner.WorkloadEnum mapWorkloadWorkload(int activeTasks) {
        if (activeTasks >= 15) {
            return MentorWorkloadResponseDataInner.WorkloadEnum.OVERLOAD;
        }
        if (activeTasks >= 8) {
            return MentorWorkloadResponseDataInner.WorkloadEnum.HIGH;
        }
        return MentorWorkloadResponseDataInner.WorkloadEnum.NORMAL;
    }

    private Long parseLong(String value) {
        String trimmed = StringUtils.trimToNull(value);
        if (trimmed == null) {
            log.warn("Report filter id is blank: raw='{}'", value);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected a numeric id");
        }
        try {
            return Long.parseLong(trimmed);
        }
        catch (NumberFormatException ex) {
            log.warn("Invalid numeric filter value: raw='{}', trimmed='{}'", value, trimmed);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid numeric value: " + trimmed);
        }
    }

    private Long parseOptionalLongQueryParam(String value, String paramName) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return Long.parseLong(trimmed);
        }
        catch (NumberFormatException ex) {
            log.warn("Invalid optional numeric filter: param='{}', raw='{}', trimmed='{}'", paramName, value, trimmed);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "Invalid " + paramName + " (expected integer): " + trimmed);
        }
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseLong(value);
    }

    private static double doubleOrZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private String fullName(UserEntity user) {
        StringBuilder sb = new StringBuilder();
        if (user.getLastName() != null) {
            sb.append(user.getLastName()).append(' ');
        }
        if (user.getFirstName() != null) {
            sb.append(user.getFirstName());
        }
        return sb.toString().trim();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private int zero(Integer value) {
        return value != null ? value : 0;
    }

    private double round(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}
