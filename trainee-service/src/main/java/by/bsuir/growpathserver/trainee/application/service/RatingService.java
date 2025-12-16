package by.bsuir.growpathserver.trainee.application.service;

import java.util.List;

import by.bsuir.growpathserver.trainee.application.query.GetInternRatingQuery;
import by.bsuir.growpathserver.trainee.application.query.GetRatingsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Rating;

public interface RatingService {
    Rating getInternRating(GetInternRatingQuery query);

    List<Rating> getRatings(GetRatingsQuery query);
}
