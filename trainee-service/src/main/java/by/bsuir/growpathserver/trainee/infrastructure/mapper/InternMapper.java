package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import by.bsuir.growpathserver.dto.model.InternResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;

@Mapper(componentModel = "spring")
public interface InternMapper {
    @Mapping(target = "id", expression = "java(String.valueOf(user.getId()))")
    @Mapping(target = "userId", expression = "java(String.valueOf(user.getId()))")
    @Mapping(source = "email.value", target = "email")
    @Mapping(source = "status", target = "status", qualifiedByName = "userStatusToInternStatus")
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "internshipProgramId", ignore = true)
    @Mapping(target = "mentorId", ignore = true)
    @Mapping(target = "rating", ignore = true)
    InternResponse toInternResponse(User user);

    @Named("userStatusToInternStatus")
    default InternResponse.StatusEnum userStatusToInternStatus(UserStatus status) {
        if (status == null) {
            return InternResponse.StatusEnum.ACTIVE;
        }
        return switch (status) {
            case ACTIVE -> InternResponse.StatusEnum.ACTIVE;
            case BLOCKED, PENDING -> InternResponse.StatusEnum.PAUSED;
        };
    }
}
