package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.RatingsApi;
import by.bsuir.growpathserver.dto.model.RatingListResponse;
import by.bsuir.growpathserver.dto.model.RatingResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RatingController implements RatingsApi {

    @Override
    public ResponseEntity<RatingResponse> getInternRating(String internId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<RatingListResponse> getRatings(String internshipId, String sortBy, String order) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
