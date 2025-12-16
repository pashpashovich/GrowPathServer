package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import by.bsuir.growpathserver.dto.model.RatingListResponse;
import by.bsuir.growpathserver.dto.model.RatingResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.Rating;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    default RatingResponse toRatingResponse(Rating rating) {
        RatingResponse response = new RatingResponse();
        response.setId(rating.getId() != null ? String.valueOf(rating.getId()) : null);
        response.setInternId(String.valueOf(rating.getInternId()));
        response.setInternName(rating.getInternName());
        response.setPosition(rating.getPosition());
        response.setMentorName(rating.getMentorName());
        response.setOverallRating(rating.getOverallRating());
        response.setQualityRating(rating.getQualityRating());
        response.setSpeedRating(rating.getSpeedRating());
        response.setCommunicationRating(rating.getCommunicationRating());
        response.setExperience(rating.getExperience());
        response.setTasksCompleted(rating.getTasksCompleted());
        response.setTasksOnTime(rating.getTasksOnTime());
        response.setAverageTaskTime(rating.getAverageTaskTime());
        response.setLastUpdated(rating.getLastUpdated());
        response.setTrend(convertTrend(rating.getTrend()));
        response.setPreviousRating(rating.getPreviousRating());
        response.setRank(rating.getRank());
        response.setInternshipId(rating.getInternshipId() != null ? String.valueOf(rating.getInternshipId()) : null);
        return response;
    }

    default RatingListResponse toRatingListResponse(List<Rating> ratings) {
        RatingListResponse response = new RatingListResponse();
        response.setData(ratings.stream()
                                 .map(this::toRatingResponse)
                                 .collect(Collectors.toList()));
        return response;
    }

    default RatingResponse.TrendEnum convertTrend(String trend) {
        if (trend == null) {
            return RatingResponse.TrendEnum.STABLE;
        }
        return switch (trend.toLowerCase()) {
            case "up" -> RatingResponse.TrendEnum.UP;
            case "down" -> RatingResponse.TrendEnum.DOWN;
            default -> RatingResponse.TrendEnum.STABLE;
        };
    }
}
