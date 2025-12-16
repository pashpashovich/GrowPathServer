package by.bsuir.growpathserver.trainee.domain.aggregate;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Rating {
    private final Long id;
    private final Long internId;
    private final String internName;
    private final String position;
    private final String mentorName;
    private final Double overallRating;
    private final Double qualityRating;
    private final Double speedRating;
    private final Double communicationRating;
    private final Integer experience;
    private final Integer tasksCompleted;
    private final Integer tasksOnTime;
    private final Double averageTaskTime;
    private final LocalDateTime lastUpdated;
    private final String trend;
    private final Double previousRating;
    private final Integer rank;
    private final Long internshipId;
}
