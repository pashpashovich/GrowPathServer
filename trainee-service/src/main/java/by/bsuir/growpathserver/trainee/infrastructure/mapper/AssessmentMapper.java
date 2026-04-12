package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.AssessmentResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssessmentMapper {
    @Mapping(target = "internName", ignore = true)
    @Mapping(target = "mentorName", ignore = true)
    AssessmentResponse toAssessmentResponse(Assessment assessment);
}
