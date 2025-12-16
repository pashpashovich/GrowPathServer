package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import by.bsuir.growpathserver.dto.model.AssessmentResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {
    @Mapping(target = "id", expression = "java(String.valueOf(assessment.getId()))")
    @Mapping(target = "internName", ignore = true)
    @Mapping(target = "mentorName", ignore = true)
    AssessmentResponse toAssessmentResponse(Assessment assessment);
}
