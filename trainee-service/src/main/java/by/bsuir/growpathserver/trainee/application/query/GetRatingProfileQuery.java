package by.bsuir.growpathserver.trainee.application.query;

public record GetRatingProfileQuery(
        Long internId,
        int historyLimit,
        int recentTasksLimit
) {
}
