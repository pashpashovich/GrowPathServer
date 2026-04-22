package by.bsuir.growpathserver.trainee.domain.events;

import java.time.LocalDateTime;

public record TaskReviewResultEvent(
        Long taskId,
        Long assigneeId,
        String status,
        Integer score,
        String feedback,
        LocalDateTime reviewedAt
) {
}
