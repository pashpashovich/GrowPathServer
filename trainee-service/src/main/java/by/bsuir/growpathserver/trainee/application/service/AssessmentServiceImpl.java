package by.bsuir.growpathserver.trainee.application.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.CreateAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateAssessmentCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;

    @Override
    @Transactional
    public Assessment createAssessment(CreateAssessmentCommand command) {
        Assessment assessment = Assessment.create(
                command.internId(),
                command.mentorId(),
                command.internshipId(),
                command.overallRating(),
                command.qualityRating(),
                command.speedRating(),
                command.communicationRating(),
                command.comment()
        );

        AssessmentEntity entity = assessment.toEntity();
        AssessmentEntity savedEntity = assessmentRepository.save(entity);
        return Assessment.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public Assessment updateAssessment(UpdateAssessmentCommand command) {
        AssessmentEntity entity = assessmentRepository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("Assessment not found with id: " + command.id()));

        if (command.overallRating() != null) {
            entity.setOverallRating(command.overallRating());
        }
        if (command.qualityRating() != null) {
            entity.setQualityRating(command.qualityRating());
        }
        if (command.speedRating() != null) {
            entity.setSpeedRating(command.speedRating());
        }
        if (command.communicationRating() != null) {
            entity.setCommunicationRating(command.communicationRating());
        }
        if (command.comment() != null) {
            entity.setComment(command.comment());
        }

        AssessmentEntity savedEntity = assessmentRepository.save(entity);
        return Assessment.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public void deleteAssessment(DeleteAssessmentCommand command) {
        if (!assessmentRepository.existsById(command.id())) {
            throw new NoSuchElementException("Assessment not found with id: " + command.id());
        }
        assessmentRepository.deleteById(command.id());
    }

    @Override
    @Transactional(readOnly = true)
    public Assessment getAssessmentById(Long id) {
        AssessmentEntity entity = assessmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Assessment not found with id: " + id));
        return Assessment.fromEntity(entity);
    }
}
