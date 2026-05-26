package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.TaskResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {
    @Mapping(target = "assigneeName", ignore = true)
    @Mapping(target = "mentorName", ignore = true)
    @Mapping(target = "iprId", ignore = true)
    @Mapping(target = "submissionFiles", ignore = true)
    @Mapping(target = "submissionLinks", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "competencyRefs", ignore = true)
    TaskResponse toTaskResponse(Task task);
}
