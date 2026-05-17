package by.bsuir.growpathserver.trainee.application.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData.DeadlineRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData.InternProgressRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData.MentorWorkloadRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData.ProgramInfo;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData.SummaryInfo;
import by.bsuir.growpathserver.trainee.application.exception.InternshipProgramNotFoundForReportException;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipEfficiencyReportAggregationService {

    public static final String REPORT_ID = "GP-RPT-2";
    private static final double BEHIND_SCHEDULE_THRESHOLD_PERCENT = 50.0;

    private final InternshipProgramRepository internshipProgramRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;

    @Transactional(readOnly = true)
    public InternshipEfficiencyReportData aggregate(Long programId) {
        InternshipProgramEntity program = internshipProgramRepository.findWithCollectionsById(programId)
                .orElseThrow(() -> new InternshipProgramNotFoundForReportException(programId));

        List<TaskEntity> tasks = taskRepository.findByInternshipId(programId);
        Map<Long, UserEntity> usersById = userRepository.findAll().stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));

        LocalDate programEnd = program.getStartDate().plusMonths(program.getDuration());
        SummaryInfo summary = buildSummary(tasks);
        List<MentorWorkloadRow> mentorWorkload = buildMentorWorkload(tasks, usersById);
        List<InternProgressRow> internProgress = buildInternProgress(tasks, usersById);
        List<DeadlineRow> deadlines = buildDeadlines(tasks, usersById);

        String generatedByName = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .flatMap(userRepository::findById)
                .map(user -> User.fromEntity(user).getDisplayName())
                .orElse("Система");

        return new InternshipEfficiencyReportData(
                REPORT_ID,
                LocalDateTime.now(),
                generatedByName,
                new ProgramInfo(
                        program.getId(),
                        program.getTitle(),
                        program.getItDirection() != null ? program.getItDirection().getDisplayName() : "—",
                        program.getStartDate(),
                        programEnd
                ),
                summary,
                mentorWorkload,
                internProgress,
                deadlines
        );
    }

    private SummaryInfo buildSummary(List<TaskEntity> tasks) {
        int total = tasks.size();
        int completed = countByStatus(tasks, TaskStatus.COMPLETED);
        int inProgress = countByStatus(tasks, TaskStatus.IN_PROGRESS);
        int pendingReviews = countByStatus(tasks, TaskStatus.ON_REVIEW);
        int overdue = countOverdue(tasks);
        double completionRate = total == 0 ? 0.0 : (completed * 100.0) / total;
        double avgReviewHours = tasks.stream()
                .filter(task -> task.getSubmittedAt() != null && task.getReviewedAt() != null)
                .mapToLong(task -> Duration.between(task.getSubmittedAt(), task.getReviewedAt()).toHours())
                .average()
                .orElse(0.0);
        return new SummaryInfo(
                total,
                completed,
                inProgress,
                overdue,
                pendingReviews,
                round(completionRate),
                round(avgReviewHours)
        );
    }

    private List<MentorWorkloadRow> buildMentorWorkload(List<TaskEntity> tasks, Map<Long, UserEntity> usersById) {
        return tasks.stream()
                .filter(task -> task.getMentorId() != null)
                .collect(Collectors.groupingBy(TaskEntity::getMentorId))
                .entrySet()
                .stream()
                .map(entry -> {
                    Long mentorId = entry.getKey();
                    List<TaskEntity> mentorTasks = entry.getValue();
                    int activeTasks = (int) mentorTasks.stream()
                            .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS
                                    || task.getStatus() == TaskStatus.ON_REVIEW
                                    || task.getStatus() == TaskStatus.SUBMITTED)
                            .count();
                    int pendingReviews = (int) mentorTasks.stream()
                            .filter(task -> task.getStatus() == TaskStatus.ON_REVIEW)
                            .count();
                    Set<Long> interns = mentorTasks.stream()
                            .map(TaskEntity::getAssigneeId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    double avgReviewHours = mentorTasks.stream()
                            .filter(task -> task.getSubmittedAt() != null && task.getReviewedAt() != null)
                            .mapToLong(task -> Duration.between(task.getSubmittedAt(), task.getReviewedAt()).toHours())
                            .average()
                            .orElse(0.0);
                    UserEntity mentor = usersById.get(mentorId);
                    return new MentorWorkloadRow(
                            mentorId,
                            mentor != null ? displayName(mentor) : "Ментор " + mentorId,
                            interns.size(),
                            activeTasks,
                            pendingReviews,
                            round(avgReviewHours),
                            workloadLabel(activeTasks)
                    );
                })
                .sorted(Comparator.comparing(MentorWorkloadRow::mentorName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<InternProgressRow> buildInternProgress(List<TaskEntity> tasks, Map<Long, UserEntity> usersById) {
        return tasks.stream()
                .filter(task -> task.getAssigneeId() != null)
                .collect(Collectors.groupingBy(TaskEntity::getAssigneeId))
                .entrySet()
                .stream()
                .map(entry -> {
                    Long internId = entry.getKey();
                    List<TaskEntity> internTasks = entry.getValue();
                    int total = internTasks.size();
                    int completed = (int) internTasks.stream()
                            .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                            .count();
                    double completionRate = total == 0 ? 0.0 : (completed * 100.0) / total;
                    UserEntity intern = usersById.get(internId);
                    return new InternProgressRow(
                            internId,
                            intern != null ? displayName(intern) : "Стажёр " + internId,
                            total,
                            completed,
                            round(completionRate),
                            completionRate < BEHIND_SCHEDULE_THRESHOLD_PERCENT
                                    && internTasks.stream().anyMatch(task -> task.getStatus() != TaskStatus.COMPLETED)
                    );
                })
                .sorted(Comparator.comparing(InternProgressRow::internName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<DeadlineRow> buildDeadlines(List<TaskEntity> tasks, Map<Long, UserEntity> usersById) {
        LocalDateTime now = LocalDateTime.now();
        return tasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.REJECTED)
                .sorted(Comparator.comparing(TaskEntity::getDueDate))
                .map(task -> {
                    UserEntity assignee = task.getAssigneeId() != null ? usersById.get(task.getAssigneeId()) : null;
                    UserEntity mentor = task.getMentorId() != null ? usersById.get(task.getMentorId()) : null;
                    boolean overdue = task.getDueDate().isBefore(now);
                    return new DeadlineRow(
                            task.getId(),
                            task.getTitle(),
                            assignee != null ? displayName(assignee) : "—",
                            mentor != null ? displayName(mentor) : "—",
                            task.getDueDate(),
                            task.getStatus().getValue(),
                            overdue
                    );
                })
                .toList();
    }

    private int countByStatus(List<TaskEntity> tasks, TaskStatus status) {
        return (int) tasks.stream().filter(task -> task.getStatus() == status).count();
    }

    private int countOverdue(List<TaskEntity> tasks) {
        LocalDateTime now = LocalDateTime.now();
        return (int) tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(now))
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.REJECTED)
                .count();
    }

    private String workloadLabel(int activeTasks) {
        if (activeTasks >= 15) {
            return "Перегрузка";
        }
        if (activeTasks >= 8) {
            return "Высокая";
        }
        return "Нормальная";
    }

    private String displayName(UserEntity user) {
        return User.fromEntity(user).getDisplayName();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
