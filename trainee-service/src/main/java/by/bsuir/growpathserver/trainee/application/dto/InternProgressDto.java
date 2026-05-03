package by.bsuir.growpathserver.trainee.application.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternProgressDto {
    private Long iprId;
    private Long internId;
    private Double overallProgress;
    private Integer completedTasks;
    private Integer totalTasks;
    private Integer completedStages;
    private Integer totalStages;
    private ProgressStatus status;
    private LocalDate estimatedCompletionDate;
    private LocalDate plannedEndDate;
    private List<StageProgressDto> stageProgress;
    private Double averageTaskRating;

    public enum ProgressStatus {
        ON_TRACK,
        BEHIND_SCHEDULE,
        AHEAD_OF_SCHEDULE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageProgressDto {
        private Long stageId;
        private String stageTitle;
        private Double progressPercentage;
        private Integer completedTasks;
        private Integer totalTasks;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
    }
}
