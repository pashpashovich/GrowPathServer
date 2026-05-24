package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import by.bsuir.growpathserver.dto.model.HiringDecisionResponse;
import by.bsuir.growpathserver.dto.model.HiringDecisionResponse.DecisionEnum;
import by.bsuir.growpathserver.trainee.domain.entity.InternHiringDecisionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InternHiringDecisionMapper {

    default HiringDecisionResponse toResponse(InternHiringDecisionEntity entity) {
        HiringDecisionResponse response = new HiringDecisionResponse();
        response.setId(entity.getId());
        response.setInternId(entity.getIntern().getId());
        response.setProgramId(entity.getProgram().getId());
        response.setProgramTitle(entity.getProgram().getTitle());
        response.setDecision(DecisionEnum.fromValue(entity.getDecision().toApiValue()));
        response.setComment(entity.getComment());
        response.setDecidedByUserId(entity.getDecidedBy().getId());
        response.setDecidedByName(formatUserName(entity.getDecidedBy()));
        response.setDecidedAt(entity.getDecidedAt());
        if (Objects.nonNull(entity.getUpdatedAt())) {
            response.setUpdatedAt(entity.getUpdatedAt());
        }
        return response;
    }

    private static String formatUserName(UserEntity user) {
        String first = StringUtils.defaultString(user.getFirstName());
        String last = StringUtils.defaultString(user.getLastName());
        String patronymic = user.getPatronymicName();
        if (StringUtils.isNotBlank(patronymic)) {
            return (first + " " + patronymic + " " + last).trim();
        }
        return (first + " " + last).trim();
    }
}
