package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetInternRatingQuery;
import by.bsuir.growpathserver.trainee.application.service.RatingService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Rating;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternRatingHandler {

    private final RatingService ratingService;

    public Rating handle(GetInternRatingQuery query) {
        return ratingService.getInternRating(query);
    }
}
