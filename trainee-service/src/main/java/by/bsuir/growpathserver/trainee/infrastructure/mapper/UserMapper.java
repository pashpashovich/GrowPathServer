package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import by.bsuir.growpathserver.dto.model.UserResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    @Mapping(target = "id", expression = "java(String.valueOf(user.getId()))")
    @Mapping(source = "email.value", target = "email")
    @Mapping(source = "invitedBy", target = "invitedBy", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    public abstract UserResponse toUserResponse(User user);

    public abstract List<UserResponse> toUserResponseList(List<User> users);
}
