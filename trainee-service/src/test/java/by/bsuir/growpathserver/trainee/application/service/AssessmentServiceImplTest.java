package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceImplTest {

    @Mock
    private AssessmentRepository assessmentRepository;

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
        existingAssessmentEntity.setOverallRating(4.5);
        existingAssessmentEntity.setQualityRating(4.0);
        existingAssessmentEntity.setSpeedRating(5.0);
        existingAssessmentEntity.setCommunicationRating(4.5);
        existingAssessmentEntity.setComment("Good performance");
    }

    @Test
    void shouldCreateAssessmentSuccessfully() {
        // Given
        CreateAssessmentCommand command = CreateAssessmentCommand.builder()
                .internId(10L)
                .mentorId(20L)
                .internshipId(30L)
                .overallRating(4.5)
                .qualityRating(4.0)
                .speedRating(5.0)
                .communicationRating(4.5)
                .comment("Excellent work")
                .build();

        when(assessmentRepository.save(any(AssessmentEntity.class))).thenAnswer(invocation -> {
            AssessmentEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // When
        Assessment result = assessmentService.createAssessment(command);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getInternId());
        assertEquals(20L, result.getMentorId());
        assertEquals(30L, result.getInternshipId());
        assertEquals(4.5, result.getOverallRating());
        assertEquals(4.0, result.getQualityRating());
        assertEquals(5.0, result.getSpeedRating());
        assertEquals(4.5, result.getCommunicationRating());
        assertEquals("Excellent work", result.getComment());
        verify(assessmentRepository).save(any(AssessmentEntity.class));
    }

    @Test
    void shouldCreateAssessmentWithNullComment() {
        // Given
        CreateAssessmentCommand command = CreateAssessmentCommand.builder()
                .internId(10L)
                .mentorId(20L)
                .internshipId(30L)
                .overallRating(3.5)
                .qualityRating(3.0)
                .speedRating(4.0)
                .communicationRating(3.5)
                .comment(null)
                .build();

        when(assessmentRepository.save(any(AssessmentEntity.class))).thenAnswer(invocation -> {
            AssessmentEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // When
        Assessment result = assessmentService.createAssessment(command);

        // Then
        assertNotNull(result);
        assertEquals(3.5, result.getOverallRating());
        verify(assessmentRepository).save(any(AssessmentEntity.class));
    }

    @Test
    void shouldUpdateAssessmentSuccessfully() {
        // Given
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

        // When
        Assessment result = assessmentService.updateAssessment(command);

        // Then
        assertNotNull(result);
        assertEquals(5.0, existingAssessmentEntity.getOverallRating());
        assertEquals(5.0, existingAssessmentEntity.getQualityRating());
        assertEquals(5.0, existingAssessmentEntity.getSpeedRating());
        assertEquals(5.0, existingAssessmentEntity.getCommunicationRating());
        assertEquals("Outstanding performance", existingAssessmentEntity.getComment());
        verify(assessmentRepository).findById(1L);
        verify(assessmentRepository).save(any(AssessmentEntity.class));
    }

    @Test
    void shouldUpdateAssessmentWithPartialFields() {
        // Given
        UpdateAssessmentCommand command = UpdateAssessmentCommand.builder()
                .id(1L)
                .overallRating(4.8)
                .qualityRating(null)
                .speedRating(null)
                .communicationRating(null)
                .comment(null)
                .build();

        when(assessmentRepository.findById(1L)).thenReturn(Optional.of(existingAssessmentEntity));
        when(assessmentRepository.save(any(AssessmentEntity.class))).thenReturn(existingAssessmentEntity);

        // When
        Assessment result = assessmentService.updateAssessment(command);

        // Then
        assertNotNull(result);
        assertEquals(4.8, existingAssessmentEntity.getOverallRating());
        assertEquals(4.0, existingAssessmentEntity.getQualityRating()); // Should remain unchanged
        verify(assessmentRepository).findById(1L);
        verify(assessmentRepository).save(any(AssessmentEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentAssessment() {
        // Given
        UpdateAssessmentCommand command = UpdateAssessmentCommand.builder()
                .id(999L)
                .overallRating(5.0)
                .build();

        when(assessmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> assessmentService.updateAssessment(command));
        verify(assessmentRepository).findById(999L);
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    void shouldDeleteAssessmentSuccessfully() {
        // Given
        DeleteAssessmentCommand command = new DeleteAssessmentCommand(1L);

        when(assessmentRepository.existsById(1L)).thenReturn(true);

        // When
        assessmentService.deleteAssessment(command);

        // Then
        verify(assessmentRepository).existsById(1L);
        verify(assessmentRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentAssessment() {
        // Given
        DeleteAssessmentCommand command = new DeleteAssessmentCommand(999L);

        when(assessmentRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> assessmentService.deleteAssessment(command));
        verify(assessmentRepository).existsById(999L);
        verify(assessmentRepository, never()).deleteById(any());
    }

    @Test
    void shouldGetAssessmentByIdSuccessfully() {
        // Given
        when(assessmentRepository.findById(1L)).thenReturn(Optional.of(existingAssessmentEntity));

        // When
        Assessment result = assessmentService.getAssessmentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(10L, result.getInternId());
        assertEquals(4.5, result.getOverallRating());
        verify(assessmentRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentAssessment() {
        // Given
        when(assessmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> assessmentService.getAssessmentById(999L));
        verify(assessmentRepository).findById(999L);
    }
}

