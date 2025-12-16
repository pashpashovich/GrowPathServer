package by.bsuir.growpathserver.trainee.application.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetRatingsQuery;
import by.bsuir.growpathserver.trainee.application.service.RatingService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Rating;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetRatingsHandler {

    private final RatingService ratingService;

    public List<Rating> handle(GetRatingsQuery query) {
        return ratingService.getRatings(query);
    }
}
