package by.bsuir.growpathserver.trainee.application.query;

public record GetRatingsQuery(
        Long internshipId,
        String sortBy,
        String order
) {
}
