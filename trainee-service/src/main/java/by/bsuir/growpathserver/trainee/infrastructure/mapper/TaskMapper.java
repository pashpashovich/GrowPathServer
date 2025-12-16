package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import by.bsuir.growpathserver.dto.model.TaskResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "id", expression = "java(String.valueOf(task.getId()))")
    @Mapping(target = "assigneeName", ignore = true)
    @Mapping(target = "mentorName", ignore = true)
    @Mapping(target = "submissionFiles", ignore = true)
    @Mapping(target = "submissionLinks", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "comments", ignore = true)
    TaskResponse toTaskResponse(Task task);
}
