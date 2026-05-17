package by.bsuir.growpathserver.trainee.application.dto.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InternshipEfficiencyReportData(
        String reportId,
        LocalDateTime generatedAt,
        String generatedByName,
        ProgramInfo program,
        SummaryInfo summary,
        List<MentorWorkloadRow> mentorWorkload,
        List<InternProgressRow> internProgress,
        List<DeadlineRow> deadlines
) {

    public record ProgramInfo(
            Long id,
            String title,
            String directionName,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public record SummaryInfo(
            int totalTasks,
            int completedTasks,
            int inProgressTasks,
            int overdueTasks,
            int pendingReviews,
            double completionRatePercent,
            double averageReviewHours
    ) {
    }

    public record MentorWorkloadRow(
            Long mentorId,
            String mentorName,
            int totalInterns,
            int activeTasks,
            int pendingReviews,
            double averageReviewHours,
            String workloadLabel
    ) {
    }

    public record InternProgressRow(
            Long internId,
            String internName,
            int totalTasks,
            int completedTasks,
            double completionRatePercent,
            boolean behindSchedule
    ) {
    }

    public record DeadlineRow(
            Long taskId,
            String title,
            String assigneeName,
            String mentorName,
            LocalDateTime dueDate,
            String status,
            boolean overdue
    ) {
    }
}
