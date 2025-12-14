package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.time.OffsetDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import by.bsuir.growpathserver.dto.model.UserResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    @Mapping(source = "email.value", target = "email")
    @Mapping(source = "role.value", target = "role")
    @Mapping(source = "status.value", target = "status")
    @Mapping(source = "createdAt", target = "createdAt", ignore = true)
    @Mapping(source = "lastLogin", target = "lastLogin", ignore = true)
    @Mapping(source = "invitedBy", target = "invitedBy", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "invitationSentAt", target = "invitationSentAt", ignore = true)
    public abstract UserResponse toUserResponse(User user);

    @AfterMapping
    protected void convertDates(@MappingTarget UserResponse response, User user) {
        if (user.getCreatedAt() != null) {
            response.setCreatedAt(user.getCreatedAt().toLocalDateTime());
        }
        if (user.getLastLogin() != null) {
            response.setLastLogin(user.getLastLogin().toLocalDateTime());
        }
        if (user.getInvitationSentAt() != null) {
            response.setInvitationSentAt(user.getInvitationSentAt().toLocalDateTime());
        }
    }

    public abstract List<UserResponse> toUserResponseList(List<User> users);
}
