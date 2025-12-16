package by.bsuir.growpathserver.trainee.application.query;

public record GetMentorsQuery(
        Integer page,
        Integer limit,
        String search
) {
}
