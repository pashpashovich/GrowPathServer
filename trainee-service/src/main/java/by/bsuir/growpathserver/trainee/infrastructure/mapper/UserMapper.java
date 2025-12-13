package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import by.bsuir.growpathserver.trainee.dto.model.users.UserResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "email.value", target = "email")
    @Mapping(source = "role.value", target = "role")
    @Mapping(source = "status.value", target = "status")
    @Mapping(source = "lastLogin", target = "lastLogin", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "invitedBy", target = "invitedBy", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "invitationSentAt", target = "invitationSentAt", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);
}
