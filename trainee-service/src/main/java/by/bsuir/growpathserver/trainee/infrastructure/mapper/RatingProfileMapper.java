package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.RatingProfileCohort;
import by.bsuir.growpathserver.dto.model.RatingProfileCurrent;
import by.bsuir.growpathserver.dto.model.RatingProfileHistoryPoint;
import by.bsuir.growpathserver.dto.model.RatingProfileRatedTask;
import by.bsuir.growpathserver.dto.model.RatingProfileResponse;
import by.bsuir.growpathserver.dto.model.RatingProfileTasks;
import by.bsuir.growpathserver.trainee.application.dto.RatingProfileDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RatingProfileMapper {

    RatingProfileResponse toRatingProfileResponse(RatingProfileDto dto);

    @Mapping(target = "trend", source = "trend", qualifiedByName = "toTrendEnum")
    RatingProfileCurrent toRatingProfileCurrent(RatingProfileDto.CurrentAssessment current);

    RatingProfileCohort toRatingProfileCohort(RatingProfileDto.CohortContext cohort);

    RatingProfileTasks toRatingProfileTasks(RatingProfileDto.TasksSummary tasks);

    RatingProfileHistoryPoint toRatingProfileHistoryPoint(RatingProfileDto.HistoryPoint point);

    RatingProfileRatedTask toRatingProfileRatedTask(RatingProfileDto.RatedTaskSummary task);

    @Named("toTrendEnum")
    default RatingProfileCurrent.TrendEnum toTrendEnum(String trend) {
        if (trend == null) {
            return RatingProfileCurrent.TrendEnum.STABLE;
        }
        return switch (trend.toLowerCase()) {
            case "up" -> RatingProfileCurrent.TrendEnum.UP;
            case "down" -> RatingProfileCurrent.TrendEnum.DOWN;
            default -> RatingProfileCurrent.TrendEnum.STABLE;
        };
    }
}
