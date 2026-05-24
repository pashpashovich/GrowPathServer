package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import by.bsuir.growpathserver.dto.model.HiringDecisionResponse;
import by.bsuir.growpathserver.dto.model.HiringDecisionResponse.DecisionEnum;
import by.bsuir.growpathserver.dto.model.HiringDecisionResponse.InternStatusEnum;
import by.bsuir.growpathserver.dto.model.HiringDecisionResponse.SystemRecommendationEnum;
import by.bsuir.growpathserver.trainee.application.service.HiringRecommendationService.Recommendation;
import by.bsuir.growpathserver.trainee.domain.entity.InternHiringDecisionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.HiringDecisionType;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternProfileStatus;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InternHiringDecisionMapper {

    default HiringDecisionResponse toView(
            InternHiringDecisionEntity entity,
            Recommendation recommendation,
            InternProfileStatus internStatus) {
        HiringDecisionResponse response = baseResponse(entity.getIntern().getId(), entity.getProgram(), recommendation, internStatus);
        response.setId(entity.getId());
        response.setRecorded(true);
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

    default HiringDecisionResponse toPreview(
            Long internId,
            Long programId,
            InternshipProgramEntity program,
            Recommendation recommendation,
            InternProfileStatus internStatus) {
        HiringDecisionResponse response = baseResponse(internId, program, recommendation, internStatus);
        response.setRecorded(false);
        return response;
    }

    private static HiringDecisionResponse baseResponse(
            Long internId,
            InternshipProgramEntity program,
            Recommendation recommendation,
            InternProfileStatus internStatus) {
        HiringDecisionResponse response = new HiringDecisionResponse();
        response.setInternId(internId);
        response.setProgramId(program.getId());
        response.setProgramTitle(program.getTitle());
        response.setSystemRecommendation(
                SystemRecommendationEnum.fromValue(recommendation.decision().toApiValue()));
        response.setSystemRecommendationReason(recommendation.reason());
        response.setInternStatus(toInternStatusEnum(internStatus));
        return response;
    }

    private static InternStatusEnum toInternStatusEnum(InternProfileStatus status) {
        return InternStatusEnum.fromValue(status.getApiValue());
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
