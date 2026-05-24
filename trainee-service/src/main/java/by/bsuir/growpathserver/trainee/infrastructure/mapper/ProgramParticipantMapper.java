package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import by.bsuir.growpathserver.dto.model.ProgramParticipantListResponse;
import by.bsuir.growpathserver.dto.model.ProgramParticipantResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramParticipantEntity;

@Mapper(componentModel = SPRING)
public interface ProgramParticipantMapper {

    default ProgramParticipantResponse toProgramParticipantResponse(InternshipProgramParticipantEntity entity) {
        User user = User.fromEntity(entity.getUser());
        ProgramParticipantResponse response = new ProgramParticipantResponse();
        response.setUserId(user.getId());
        response.setName(user.getDisplayName());
        response.setEmail(user.getEmail().value());
        response.setAssignedAt(entity.getAssignedAt());
        if (Objects.nonNull(entity.getMentor())) {
            User mentor = User.fromEntity(entity.getMentor());
            response.setMentorId(mentor.getId());
            response.setMentorName(mentor.getDisplayName());
        }
        return response;
    }

    default ProgramParticipantListResponse toProgramParticipantListResponse(
            List<InternshipProgramParticipantEntity> entities) {
        ProgramParticipantListResponse response = new ProgramParticipantListResponse();
        response.setData(entities.stream()
                                 .map(this::toProgramParticipantResponse)
                                 .collect(Collectors.toList()));
        return response;
    }
}
