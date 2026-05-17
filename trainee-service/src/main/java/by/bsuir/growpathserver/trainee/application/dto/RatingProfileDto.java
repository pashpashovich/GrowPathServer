package by.bsuir.growpathserver.trainee.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RatingProfileDto {

    private final Long internId;
    private final String internName;
    private final Long internshipId;
    private final String programName;
    private final boolean hasAssessment;
    private final CurrentAssessment current;
    private final CohortContext cohort;
    private final TasksSummary tasks;
    private final List<HistoryPoint> history;
    private final List<RatedTaskSummary> recentRatedTasks;

    @Getter
    @Builder
    public static class CurrentAssessment {
        private final Long assessmentId;
        private final Double overallRating;
        private final Double qualityRating;
        private final Double speedRating;
        private final Double communicationRating;
        private final String comment;
        private final Long mentorId;
        private final String mentorName;
        private final LocalDateTime lastUpdated;
        private final String trend;
        private final Double previousRating;
    }

    @Getter
    @Builder
    public static class CohortContext {
        private final Integer rank;
        private final int cohortSize;
        private final Double averageOverallRating;
        private final Double deltaFromAverage;
    }

    @Getter
    @Builder
    public static class TasksSummary {
        private final int completed;
        private final int onTime;
        private final double onTimePercent;
        private final double averageTaskTimeHours;
        private final double averageTaskRating;
        private final int ratedTasksCount;
    }

    @Getter
    @Builder
    public static class HistoryPoint {
        private final Long assessmentId;
        private final LocalDateTime date;
        private final Double overallRating;
        private final Double qualityRating;
        private final Double speedRating;
        private final Double communicationRating;
    }

    @Getter
    @Builder
    public static class RatedTaskSummary {
        private final Long taskId;
        private final String title;
        private final Integer rating;
        private final LocalDateTime completedAt;
        private final String feedback;
    }
}
