package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.api.RatingsApi;
import by.bsuir.growpathserver.dto.model.RatingListResponse;
import by.bsuir.growpathserver.dto.model.RatingProfileResponse;
import by.bsuir.growpathserver.dto.model.RatingResponse;
import by.bsuir.growpathserver.trainee.application.handler.GetInternRatingHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetRatingProfileHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetRatingsHandler;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.query.GetInternRatingQuery;
import by.bsuir.growpathserver.trainee.application.query.GetRatingProfileQuery;
import by.bsuir.growpathserver.trainee.application.query.GetRatingsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Rating;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.RatingMapper;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.RatingProfileMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RatingController extends BaseController implements RatingsApi {

    private final GetInternRatingHandler getInternRatingHandler;
    private final GetRatingsHandler getRatingsHandler;
    private final GetRatingProfileHandler getRatingProfileHandler;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;
    private final RatingMapper ratingMapper;
    private final RatingProfileMapper ratingProfileMapper;

    @Override
    public ResponseEntity<RatingProfileResponse> getRatingProfile(Integer historyLimit, Integer recentTasksLimit) {
        Long internId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        int history = historyLimit != null ? historyLimit : 12;
        int recentTasks = recentTasksLimit != null ? recentTasksLimit : 5;
        GetRatingProfileQuery query = new GetRatingProfileQuery(internId, history, recentTasks);
        return ResponseEntity.ok(
                ratingProfileMapper.toRatingProfileResponse(getRatingProfileHandler.handle(query)));
    }

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
