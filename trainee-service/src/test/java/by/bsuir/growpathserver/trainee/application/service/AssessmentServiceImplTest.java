package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.CreateAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.service.AssessmentIprStageBindingService.ResolvedStageBinding;
import by.bsuir.growpathserver.trainee.application.service.impl.AssessmentServiceImpl;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceImplTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private AssessmentIprStageBindingService assessmentIprStageBindingService;

    @InjectMocks
    private AssessmentServiceImpl assessmentService;

    private AssessmentEntity existingAssessmentEntity;

    @BeforeEach
    void setUp() {
        existingAssessmentEntity = new AssessmentEntity();
        existingAssessmentEntity.setId(1L);
        existingAssessmentEntity.setInternId(10L);
        existingAssessmentEntity.setMentorId(20L);
        existingAssessmentEntity.setInternshipId(30L);
        existingAssessmentEntity.setIprId(100L);
        existingAssessmentEntity.setIprStageId(200L);
        existingAssessmentEntity.setOverallRating(4.5);
        existingAssessmentEntity.setQualityRating(4.0);
        existingAssessmentEntity.setSpeedRating(5.0);
        existingAssessmentEntity.setCommunicationRating(4.5);
        existingAssessmentEntity.setComment("Good performance");
    }

    @Test
    void shouldCreateAssessmentSuccessfully() {
        CreateAssessmentCommand command = CreateAssessmentCommand.builder()
                .internId(10L)
                .mentorId(20L)
                .internshipId(30L)
                .iprStageId(200L)
                .overallRating(4.5)
                .qualityRating(4.0)
                .speedRating(5.0)
                .communicationRating(4.5)
                .comment("Excellent work")
                .build();

        when(assessmentIprStageBindingService.resolveRequired(eq(200L), eq(10L), eq(30L)))
                .thenReturn(new ResolvedStageBinding(100L, 30L, "Stage 1"));
        when(assessmentRepository.save(any(AssessmentEntity.class))).thenAnswer(invocation -> {
            AssessmentEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        Assessment result = assessmentService.createAssessment(command);

        assertNotNull(result);
        assertEquals(10L, result.getInternId());
        assertEquals(100L, result.getIprId());
        assertEquals(200L, result.getIprStageId());
        assertEquals(30L, result.getInternshipId());
        verify(assessmentRepository).save(any(AssessmentEntity.class));
    }

    @Test
    void shouldRejectCreateWithoutIprStageId() {
        CreateAssessmentCommand command = CreateAssessmentCommand.builder()
                .internId(10L)
                .mentorId(20L)
                .internshipId(30L)
                .overallRating(4.5)
                .build();

        assertThrows(IllegalArgumentException.class, () -> assessmentService.createAssessment(command));
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    void shouldCreateAssessmentWithNullComment() {
        CreateAssessmentCommand command = CreateAssessmentCommand.builder()
                .internId(10L)
                .mentorId(20L)
                .internshipId(30L)
                .iprStageId(200L)
                .overallRating(4.5)
                .build();

        when(assessmentIprStageBindingService.resolveRequired(eq(200L), eq(10L), eq(30L)))
                .thenReturn(new ResolvedStageBinding(100L, 30L, "Stage 1"));
        when(assessmentRepository.save(any(AssessmentEntity.class))).thenAnswer(invocation -> {
            AssessmentEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        Assessment result = assessmentService.createAssessment(command);

        assertNotNull(result);
        verify(assessmentRepository).save(any(AssessmentEntity.class));
    }

    @Test
    void shouldUpdateAssessmentSuccessfully() {
        UpdateAssessmentCommand command = UpdateAssessmentCommand.builder()
                .id(1L)
                .overallRating(5.0)
                .qualityRating(5.0)
                .speedRating(5.0)
                .communicationRating(5.0)
                .comment("Outstanding performance")
                .build();

        when(assessmentRepository.findById(1L)).thenReturn(Optional.of(existingAssessmentEntity));
        when(assessmentRepository.save(any(AssessmentEntity.class))).thenReturn(existingAssessmentEntity);

        Assessment result = assessmentService.updateAssessment(command);

        assertNotNull(result);
        assertEquals(5.0, existingAssessmentEntity.getOverallRating());
        verify(assessmentRepository).save(any(AssessmentEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentAssessment() {
        UpdateAssessmentCommand command = UpdateAssessmentCommand.builder()
                .id(999L)
                .overallRating(5.0)
                .build();

        when(assessmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> assessmentService.updateAssessment(command));
    }

    @Test
    void shouldDeleteAssessmentSuccessfully() {
        DeleteAssessmentCommand command = new DeleteAssessmentCommand(1L);
        when(assessmentRepository.existsById(1L)).thenReturn(true);

        assessmentService.deleteAssessment(command);

        verify(assessmentRepository).deleteById(1L);
    }

    @Test
    void shouldGetAssessmentByIdSuccessfully() {
        when(assessmentRepository.findById(1L)).thenReturn(Optional.of(existingAssessmentEntity));

        Assessment result = assessmentService.getAssessmentById(1L);

        assertEquals(1L, result.getId());
        assertEquals(200L, result.getIprStageId());
    }
}
