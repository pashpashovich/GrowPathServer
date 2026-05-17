package by.bsuir.growpathserver.trainee.application.dto.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InternshipResultReportData(
        String reportId,
        LocalDateTime generatedAt,
        String generatedByName,
        InternInfo intern,
        ProgramInfo program,
        IprInfo ipr,
        ProgressInfo progress,
        RatingInfo rating,
        List<CompetencyRow> competencies,
        List<CompletedTaskRow> completedTasks,
        List<AssessmentRow> assessments
) {

    public record InternInfo(Long id, String fullName) {
    }

    public record ProgramInfo(
            Long id,
            String title,
            String directionName,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public record IprInfo(Long id, String title, LocalDate startDate, LocalDate endDate, String status) {
    }

    public record ProgressInfo(
            double overallProgressPercent,
            double averageTaskRating,
            int completedTasks,
            int totalTasks
    ) {
    }

    public record RatingInfo(Double overallRating, String mentorName) {
    }

    public record CompetencyRow(String name, double achievedLevelOutOfFive) {
    }

    public record CompletedTaskRow(
            String title,
            LocalDateTime completedAt,
            Integer rating,
            String reviewComment,
            List<ArtifactLink> artifacts
    ) {
    }

    public record ArtifactLink(String name, String url) {
    }

    public record AssessmentRow(
            LocalDateTime date,
            double overallRating,
            Double qualityRating,
            Double speedRating,
            Double communicationRating,
            String comment,
            String mentorName
    ) {
    }
}
