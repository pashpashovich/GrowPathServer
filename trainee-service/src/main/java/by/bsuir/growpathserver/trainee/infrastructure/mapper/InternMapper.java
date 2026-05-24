package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.InternResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InternMapper {
    @Mapping(source = "id", target = "userId")
    @Mapping(target = "name", expression = "java(user.getDisplayName())")
    @Mapping(source = "email.value", target = "email")
    @Mapping(source = ".", target = "status", qualifiedByName = "userStatusToInternStatus")
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "internshipProgramId", ignore = true)
    @Mapping(target = "mentorId", ignore = true)
    @Mapping(target = "rating", ignore = true)
    InternResponse toInternResponse(User user);

    @Named("userStatusToInternStatus")
    default InternResponse.StatusEnum userStatusToInternStatus(User user) {
        if (user.getRole() == UserRole.INTERN && user.getInternProfileStatus() != null) {
            return switch (user.getInternProfileStatus()) {
                case ACTIVE -> InternResponse.StatusEnum.ACTIVE;
                case ADDITIONAL_ASSESSMENT -> InternResponse.StatusEnum.ADDITIONAL_ASSESSMENT;
                case COMPLETED -> InternResponse.StatusEnum.COMPLETED;
                case PAUSED -> InternResponse.StatusEnum.PAUSED;
            };
        }
        UserStatus status = user.getStatus();
        if (status == null) {
            return InternResponse.StatusEnum.ACTIVE;
        }
        return switch (status) {
            case ACTIVE -> InternResponse.StatusEnum.ACTIVE;
            case BLOCKED, PENDING -> InternResponse.StatusEnum.PAUSED;
        };
    }
}
