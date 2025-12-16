package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import by.bsuir.growpathserver.dto.model.DepartmentResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.Department;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    @Mapping(target = "id", expression = "java(String.valueOf(department.getId()))")
    DepartmentResponse toDepartmentResponse(Department department);
}
