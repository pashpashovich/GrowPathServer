package by.bsuir.growpathserver.trainee.application.service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.query.GetInternRatingQuery;
import by.bsuir.growpathserver.trainee.application.query.GetRatingsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.domain.aggregate.Rating;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final AssessmentRepository assessmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

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
        String mentorName = mentor != null ? mentor.getName() : null;

        return Rating.builder()
                .id(assessment.getId())
                .internId(assessment.getInternId())
                .internName(intern.getName())
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
