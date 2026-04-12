package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.InternResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InternMapper {
    @Mapping(source = "id", target = "userId")
    @Mapping(target = "name", expression = "java(user.getDisplayName())")
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
