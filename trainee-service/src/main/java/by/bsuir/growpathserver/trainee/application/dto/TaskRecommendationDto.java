package by.bsuir.growpathserver.trainee.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRecommendationDto {
    private Long taskId;
    private String title;
    private String description;
    private String priority;
    private LocalDateTime dueDate;
    private Double relevanceScore;
    private String recommendationReason;
    private Integer difficultyLevel;
    private Integer estimatedHours;
    private List<String> relatedCompetencies;
    private Long stageId;
    private String stageTitle;
    private Long daysUntilDue;
}
