package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.RatingsApi;
import by.bsuir.growpathserver.dto.model.RatingListResponse;
import by.bsuir.growpathserver.dto.model.RatingResponse;
import by.bsuir.growpathserver.trainee.application.handler.GetInternRatingHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetRatingsHandler;
import by.bsuir.growpathserver.trainee.application.query.GetInternRatingQuery;
import by.bsuir.growpathserver.trainee.application.query.GetRatingsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Rating;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.RatingMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RatingController implements RatingsApi {

    private final GetInternRatingHandler getInternRatingHandler;
    private final GetRatingsHandler getRatingsHandler;
    private final RatingMapper ratingMapper;

    @Override
    public ResponseEntity<RatingResponse> getInternRating(String internId) {
        try {
            Long internIdLong = Long.parseLong(internId);
            GetInternRatingQuery query = new GetInternRatingQuery(internIdLong);
            Rating rating = getInternRatingHandler.handle(query);
            RatingResponse response = ratingMapper.toRatingResponse(rating);
            return ResponseEntity.ok(response);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<RatingListResponse> getRatings(String internshipId, String sortBy, String order) {
        try {
            Long internshipIdLong = internshipId != null ? Long.parseLong(internshipId) : null;
            GetRatingsQuery query = new GetRatingsQuery(internshipIdLong, sortBy, order);
            List<Rating> ratings = getRatingsHandler.handle(query);
            RatingListResponse response = ratingMapper.toRatingListResponse(ratings);
            return ResponseEntity.ok(response);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
