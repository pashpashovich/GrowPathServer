package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.Objects;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.AssessmentResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprStageRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssessmentResponseEnricher {

    private final AssessmentMapper assessmentMapper;
    private final UserRepository userRepository;
    private final IprStageRepository iprStageRepository;

    public AssessmentResponse toAssessmentResponse(Assessment assessment) {
        AssessmentResponse response = assessmentMapper.toAssessmentResponse(assessment);
        response.setIprId(assessment.getIprId());
        response.setIprStageId(assessment.getIprStageId());

        if (Objects.nonNull(assessment.getInternId())) {
            userRepository.findById(assessment.getInternId())
                    .map(User::fromEntity)
                    .ifPresent(user -> response.setInternName(user.getDisplayName()));
        }
        if (Objects.nonNull(assessment.getMentorId())) {
            userRepository.findById(assessment.getMentorId())
                    .map(User::fromEntity)
                    .ifPresent(user -> response.setMentorName(user.getDisplayName()));
        }
        if (Objects.nonNull(assessment.getIprStageId())) {
            iprStageRepository.findById(assessment.getIprStageId())
                    .ifPresent(stage -> response.setIprStageTitle(stage.getTitle()));
        }
        return response;
    }
}
