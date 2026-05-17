package by.bsuir.growpathserver.trainee.application.service.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.dto.RatingProfileDto;
import by.bsuir.growpathserver.trainee.application.query.GetInternRatingQuery;
import by.bsuir.growpathserver.trainee.application.query.GetRatingProfileQuery;
import by.bsuir.growpathserver.trainee.application.query.GetRatingsQuery;
import by.bsuir.growpathserver.trainee.application.service.RatingService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.domain.aggregate.Rating;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final AssessmentRepository assessmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final InternshipProgramRepository internshipProgramRepository;
    private final IprRepository iprRepository;

    @Override
    @Transactional(readOnly = true)
    public Rating getInternRating(GetInternRatingQuery query) {
        User intern = User.fromEntity(userRepository.findById(query.internId())
                                              .orElseThrow(() -> new NoSuchElementException(
                                                      "Intern not found with id: " + query.internId())));

        AssessmentEntity latestAssessment = assessmentRepository.findAll().stream()
                .filter(assessment -> assessment.getInternId().equals(query.internId()))
                .max(Comparator.comparing(AssessmentEntity::getUpdatedAt))
                .orElse(null);

        if (latestAssessment == null) {
            throw new NoSuchElementException("No rating found for intern with id: " + query.internId());
        }

        Assessment assessment = Assessment.fromEntity(latestAssessment);

        List<TaskEntity> tasks = taskRepository.findAll().stream()
                .filter(task -> task.getAssigneeId() != null && task.getAssigneeId().equals(query.internId()))
                .collect(Collectors.toList());

        int tasksCompleted = (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();

        int tasksOnTime = (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .filter(task -> task.getDueDate() != null && task.getCompletedAt() != null)
                .filter(task -> !task.getCompletedAt().isAfter(task.getDueDate()))
                .count();

        double averageTaskTime = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .filter(task -> task.getTakenAt() != null && task.getCompletedAt() != null)
                .mapToLong(task -> Duration.between(task.getTakenAt(), task.getCompletedAt()).toHours())
                .average()
                .orElse(0.0);

        AssessmentEntity previousAssessment = assessmentRepository.findAll().stream()
                .filter(a -> a.getInternId().equals(query.internId()))
                .filter(a -> a.getUpdatedAt().isBefore(latestAssessment.getUpdatedAt()))
                .max(Comparator.comparing(AssessmentEntity::getUpdatedAt))
                .orElse(null);

        Double previousRating = previousAssessment != null ? previousAssessment.getOverallRating() : null;
        String trend = calculateTrend(latestAssessment.getOverallRating(), previousRating);

        User mentor = userRepository.findById(assessment.getMentorId())
                .map(User::fromEntity)
                .orElse(null);
        String mentorName = mentor != null ? mentor.getDisplayName() : null;

        return Rating.builder()
                .id(assessment.getId())
                .internId(assessment.getInternId())
                .internName(intern.getDisplayName())
                .position(null)
                .mentorName(mentorName)
                .overallRating(assessment.getOverallRating())
                .qualityRating(assessment.getQualityRating())
                .speedRating(assessment.getSpeedRating())
                .communicationRating(assessment.getCommunicationRating())
                .experience(tasksCompleted)
                .tasksCompleted(tasksCompleted)
                .tasksOnTime(tasksOnTime)
                .averageTaskTime(averageTaskTime)
                .lastUpdated(assessment.getUpdatedAt())
                .trend(trend)
                .previousRating(previousRating)
                .rank(null)
                .internshipId(assessment.getInternshipId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rating> getRatings(GetRatingsQuery query) {
        List<AssessmentEntity> assessments;

        if (query.internshipId() != null) {
            assessments = assessmentRepository.findAll().stream()
                    .filter(assessment -> assessment.getInternshipId().equals(query.internshipId()))
                    .collect(Collectors.toList());
        }
        else {
            assessments = assessmentRepository.findAll();
        }

        List<AssessmentEntity> latestAssessments = assessments.stream()
                .collect(Collectors.groupingBy(AssessmentEntity::getInternId))
                .values()
                .stream()
                .map(internAssessments -> internAssessments.stream()
                        .max(Comparator.comparing(AssessmentEntity::getUpdatedAt))
                        .orElse(null))
                .filter(assessment -> assessment != null)
                .collect(Collectors.toList());

        List<Rating> ratings = latestAssessments.stream()
                .map(assessment -> {
                    try {
                        GetInternRatingQuery internQuery = new GetInternRatingQuery(assessment.getInternId());
                        return getInternRating(internQuery);
                    }
                    catch (Exception e) {
                        return null;
                    }
                })
                .filter(rating -> rating != null)
                .collect(Collectors.toList());

        if (query.sortBy() != null && query.order() != null) {
            Comparator<Rating> comparator = getComparator(query.sortBy());
            if ("desc".equalsIgnoreCase(query.order())) {
                comparator = comparator.reversed();
            }
            ratings.sort(comparator);
        }

        for (int i = 0; i < ratings.size(); i++) {
            Rating rating = ratings.get(i);
            Rating.RatingBuilder builder = Rating.builder()
                    .id(rating.getId())
                    .internId(rating.getInternId())
                    .internName(rating.getInternName())
                    .position(rating.getPosition())
                    .mentorName(rating.getMentorName())
                    .overallRating(rating.getOverallRating())
                    .qualityRating(rating.getQualityRating())
                    .speedRating(rating.getSpeedRating())
                    .communicationRating(rating.getCommunicationRating())
                    .experience(rating.getExperience())
                    .tasksCompleted(rating.getTasksCompleted())
                    .tasksOnTime(rating.getTasksOnTime())
                    .averageTaskTime(rating.getAverageTaskTime())
                    .lastUpdated(rating.getLastUpdated())
                    .trend(rating.getTrend())
                    .previousRating(rating.getPreviousRating())
                    .rank(i + 1)
                    .internshipId(rating.getInternshipId());
            ratings.set(i, builder.build());
        }

        return ratings;
    }

    @Override
    @Transactional(readOnly = true)
    public RatingProfileDto getRatingProfile(GetRatingProfileQuery query) {
        User intern = User.fromEntity(userRepository.findById(query.internId())
                                              .orElseThrow(() -> new NoSuchElementException(
                                                      "Intern not found with id: " + query.internId())));

        List<AssessmentEntity> assessments =
                assessmentRepository.findByInternIdOrderByUpdatedAtAsc(query.internId());
        List<TaskEntity> tasks = taskRepository.findByAssigneeId(query.internId());

        Long internshipId = resolveInternshipId(assessments, tasks, query.internId());
        String programName = resolveProgramName(internshipId);

        AssessmentEntity latest = assessments.isEmpty() ? null : assessments.get(assessments.size() - 1);
        AssessmentEntity previous = assessments.size() < 2 ? null : assessments.get(assessments.size() - 2);

        boolean hasAssessment = latest != null;
        RatingProfileDto.CurrentAssessment current = hasAssessment
                ? buildCurrentAssessment(latest, previous)
                : null;

        RatingProfileDto.TasksSummary tasksSummary = buildTasksSummary(tasks, query.internId());
        List<RatingProfileDto.HistoryPoint> history = buildHistory(assessments, query.historyLimit());
        List<RatingProfileDto.RatedTaskSummary> recentRatedTasks =
                buildRecentRatedTasks(tasks, query.recentTasksLimit());

        Double myOverall = hasAssessment ? latest.getOverallRating() : null;
        RatingProfileDto.CohortContext cohort = buildCohort(internshipId, query.internId(), myOverall);

        return RatingProfileDto.builder()
                .internId(intern.getId())
                .internName(intern.getDisplayName())
                .internshipId(internshipId)
                .programName(programName)
                .hasAssessment(hasAssessment)
                .current(current)
                .cohort(cohort)
                .tasks(tasksSummary)
                .history(history)
                .recentRatedTasks(recentRatedTasks)
                .build();
    }

    private Long resolveInternshipId(List<AssessmentEntity> assessments, List<TaskEntity> tasks, Long internId) {
        if (!assessments.isEmpty()) {
            return assessments.get(assessments.size() - 1).getInternshipId();
        }
        if (!tasks.isEmpty()) {
            return tasks.get(0).getInternshipId();
        }
        return iprRepository.findByInternId(internId).stream()
                .map(IprEntity::getProgram)
                .filter(Objects::nonNull)
                .map(program -> program.getId())
                .findFirst()
                .orElse(null);
    }

    private String resolveProgramName(Long internshipId) {
        if (internshipId == null) {
            return null;
        }
        return internshipProgramRepository.findById(internshipId)
                .map(program -> program.getTitle())
                .orElse(null);
    }

    private RatingProfileDto.CurrentAssessment buildCurrentAssessment(AssessmentEntity latest,
                                                                      AssessmentEntity previous) {
        Double previousRating = previous != null ? previous.getOverallRating() : null;
        User mentor = userRepository.findById(latest.getMentorId())
                .map(User::fromEntity)
                .orElse(null);

        return RatingProfileDto.CurrentAssessment.builder()
                .assessmentId(latest.getId())
                .overallRating(latest.getOverallRating())
                .qualityRating(latest.getQualityRating())
                .speedRating(latest.getSpeedRating())
                .communicationRating(latest.getCommunicationRating())
                .comment(latest.getComment())
                .mentorId(latest.getMentorId())
                .mentorName(mentor != null ? mentor.getDisplayName() : null)
                .lastUpdated(latest.getUpdatedAt())
                .trend(calculateTrend(latest.getOverallRating(), previousRating))
                .previousRating(previousRating)
                .build();
    }

    private RatingProfileDto.TasksSummary buildTasksSummary(List<TaskEntity> tasks, Long internId) {
        int completed = (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();

        int onTime = (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .filter(task -> task.getDueDate() != null && task.getCompletedAt() != null)
                .filter(task -> !task.getCompletedAt().isAfter(task.getDueDate()))
                .count();

        double onTimePercent = completed > 0 ? (onTime * 100.0) / completed : 0.0;

        double averageTaskTime = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .filter(task -> task.getTakenAt() != null && task.getCompletedAt() != null)
                .mapToLong(task -> Duration.between(task.getTakenAt(), task.getCompletedAt()).toHours())
                .average()
                .orElse(0.0);

        int ratedTasksCount = (int) tasks.stream()
                .filter(task -> task.getRating() != null)
                .count();

        Double averageTaskRating = taskRepository.getAverageRatingByAssigneeId(internId);

        return RatingProfileDto.TasksSummary.builder()
                .completed(completed)
                .onTime(onTime)
                .onTimePercent(onTimePercent)
                .averageTaskTimeHours(averageTaskTime)
                .averageTaskRating(averageTaskRating != null ? averageTaskRating : 0.0)
                .ratedTasksCount(ratedTasksCount)
                .build();
    }

    private List<RatingProfileDto.HistoryPoint> buildHistory(List<AssessmentEntity> assessments, int historyLimit) {
        List<AssessmentEntity> slice = assessments;
        if (assessments.size() > historyLimit) {
            slice = assessments.subList(assessments.size() - historyLimit, assessments.size());
        }
        List<RatingProfileDto.HistoryPoint> history = new ArrayList<>();
        for (AssessmentEntity assessment : slice) {
            history.add(RatingProfileDto.HistoryPoint.builder()
                                .assessmentId(assessment.getId())
                                .date(assessment.getUpdatedAt())
                                .overallRating(assessment.getOverallRating())
                                .qualityRating(assessment.getQualityRating())
                                .speedRating(assessment.getSpeedRating())
                                .communicationRating(assessment.getCommunicationRating())
                                .build());
        }
        return history;
    }

    private List<RatingProfileDto.RatedTaskSummary> buildRecentRatedTasks(List<TaskEntity> tasks, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return tasks.stream()
                .filter(task -> task.getRating() != null)
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .sorted(Comparator.comparing(
                        TaskEntity::getCompletedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(task -> RatingProfileDto.RatedTaskSummary.builder()
                        .taskId(task.getId())
                        .title(task.getTitle())
                        .rating(task.getRating())
                        .completedAt(task.getCompletedAt())
                        .feedback(task.getReviewComment())
                        .build())
                .collect(Collectors.toList());
    }

    private RatingProfileDto.CohortContext buildCohort(Long internshipId, Long internId, Double myOverall) {
        if (internshipId == null) {
            return RatingProfileDto.CohortContext.builder()
                    .rank(null)
                    .cohortSize(0)
                    .averageOverallRating(null)
                    .deltaFromAverage(null)
                    .build();
        }

        List<Rating> cohortRatings = getRatings(new GetRatingsQuery(internshipId, "overallRating", "desc"));
        int cohortSize = cohortRatings.size();

        Double averageOverall = cohortRatings.stream()
                .map(Rating::getOverallRating)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        Integer rank = cohortRatings.stream()
                .filter(rating -> internId.equals(rating.getInternId()))
                .map(Rating::getRank)
                .findFirst()
                .orElse(null);

        Double deltaFromAverage = null;
        if (myOverall != null && cohortSize > 0) {
            deltaFromAverage = myOverall - averageOverall;
        }

        return RatingProfileDto.CohortContext.builder()
                .rank(rank)
                .cohortSize(cohortSize)
                .averageOverallRating(cohortSize > 0 ? averageOverall : null)
                .deltaFromAverage(deltaFromAverage)
                .build();
    }

    private Comparator<Rating> getComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "overallrating" ->
                    Comparator.comparing(Rating::getOverallRating, Comparator.nullsLast(Comparator.naturalOrder()));
            case "qualityrating" ->
                    Comparator.comparing(Rating::getQualityRating, Comparator.nullsLast(Comparator.naturalOrder()));
            case "speedrating" ->
                    Comparator.comparing(Rating::getSpeedRating, Comparator.nullsLast(Comparator.naturalOrder()));
            case "communicationrating" -> Comparator.comparing(Rating::getCommunicationRating,
                                                               Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Rating::getOverallRating, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private String calculateTrend(Double currentRating, Double previousRating) {
        if (previousRating == null) {
            return "stable";
        }
        double diff = currentRating - previousRating;
        if (diff > 0.1) {
            return "up";
        }
        else if (diff < -0.1) {
            return "down";
        }
        else {
            return "stable";
        }
    }
}
