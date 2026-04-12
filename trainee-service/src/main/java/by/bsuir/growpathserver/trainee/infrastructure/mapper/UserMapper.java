package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.UserResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class UserMapper {
    @Mapping(source = "email.value", target = "email")
    @Mapping(target = "role", expression = "java(by.bsuir.growpathserver.dto.model.UserResponse.RoleEnum.fromValue(user.getRole().getValue()))")
    @Mapping(target = "status", expression = "java(by.bsuir.growpathserver.dto.model.UserResponse.StatusEnum.fromValue(user.getStatus().getValue()))")
    public abstract UserResponse toUserResponse(User user);

    public abstract List<UserResponse> toUserResponseList(List<User> users);
}
