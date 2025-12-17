package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;

@ExtendWith(MockitoExtension.class)
class InternshipProgramServiceImplTest {

    @Mock
    private InternshipProgramRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private InternshipProgramServiceImpl internshipProgramService;

    private InternshipProgramEntity existingProgramEntity;
    private LocalDate startDate;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        startDate = LocalDate.now().plusMonths(1);
        existingProgramEntity = new InternshipProgramEntity();
        existingProgramEntity.setId(1L);
        existingProgramEntity.setTitle("Spring Boot Internship");
        existingProgramEntity.setDescription("Learn Spring Boot");
        existingProgramEntity.setStartDate(startDate);
        existingProgramEntity.setDuration(3);
        existingProgramEntity.setMaxPlaces(20);
        existingProgramEntity.setStatus(InternshipProgramStatus.ACTIVE);
        existingProgramEntity.setRequirements("[\"Java\", \"Spring\"]");
        existingProgramEntity.setGoals("[]");
        existingProgramEntity.setCompetencies("[]");
        existingProgramEntity.setSelectionStages("[]");

        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("[]");
    }

    @Test
    void shouldCreateInternshipProgramSuccessfully() throws JsonProcessingException {
        // Given
        List<String> requirements = List.of("Java", "Spring Boot");
        CreateInternshipProgramCommand.ProgramGoal goal = new CreateInternshipProgramCommand.ProgramGoal(
                "Learn Spring", "Master Spring Framework");
        List<CreateInternshipProgramCommand.ProgramGoal> goals = List.of(goal);
        List<String> competencies = List.of("Backend Development");
        CreateInternshipProgramCommand.SelectionStage stage = new CreateInternshipProgramCommand.SelectionStage(
                "Interview", "Technical interview", 1);
        List<CreateInternshipProgramCommand.SelectionStage> stages = List.of(stage);

        CreateInternshipProgramCommand command = CreateInternshipProgramCommand.builder()
                .title("New Internship")
                .description("New Description")
                .startDate(startDate)
                .duration(6)
                .maxPlaces(15)
                .requirements(requirements)
                .goals(goals)
                .competencies(competencies)
                .selectionStages(stages)
                .status(InternshipProgramStatus.ACTIVE)
                .createdBy(1L)
                .build();

        when(objectMapper.writeValueAsString(requirements)).thenReturn("[\"Java\", \"Spring Boot\"]");
        when(objectMapper.writeValueAsString(goals)).thenReturn("[{\"title\":\"Learn Spring\"}]");
        when(objectMapper.writeValueAsString(competencies)).thenReturn("[\"Backend Development\"]");
        when(objectMapper.writeValueAsString(stages)).thenReturn("[{\"name\":\"Interview\"}]");
        when(repository.save(any(InternshipProgramEntity.class))).thenAnswer(invocation -> {
            InternshipProgramEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // When
        InternshipProgram result = internshipProgramService.createInternshipProgram(command);

        // Then
        assertNotNull(result);
        assertEquals("New Internship", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(6, result.getDuration());
        assertEquals(15, result.getMaxPlaces());
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldCreateInternshipProgramWithNullJsonFields() throws JsonProcessingException {
        // Given
        CreateInternshipProgramCommand command = CreateInternshipProgramCommand.builder()
                .title("Simple Internship")
                .description("Simple Description")
                .startDate(startDate)
                .duration(3)
                .maxPlaces(10)
                .requirements(null)
                .goals(null)
                .competencies(null)
                .selectionStages(null)
                .status(InternshipProgramStatus.ACTIVE)
                .createdBy(1L)
                .build();

        when(repository.save(any(InternshipProgramEntity.class))).thenAnswer(invocation -> {
            InternshipProgramEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // When
        InternshipProgram result = internshipProgramService.createInternshipProgram(command);

        // Then
        assertNotNull(result);
        assertEquals("Simple Internship", result.getTitle());
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenJsonSerializationFails() throws JsonProcessingException {
        // Given
        List<String> requirements = List.of("Java");
        CreateInternshipProgramCommand command = CreateInternshipProgramCommand.builder()
                .title("Test Internship")
                .description("Test Description")
                .startDate(startDate)
                .duration(3)
                .maxPlaces(10)
                .requirements(requirements)
                .goals(null)
                .competencies(null)
                .selectionStages(null)
                .status(InternshipProgramStatus.ACTIVE)
                .createdBy(1L)
                .build();

        when(objectMapper.writeValueAsString(requirements))
                .thenThrow(new JsonProcessingException("JSON error") {
                });

        // When & Then
        assertThrows(IllegalArgumentException.class,
                     () -> internshipProgramService.createInternshipProgram(command));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateInternshipProgramSuccessfully() throws JsonProcessingException {
        // Given
        UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Description")
                .startDate(startDate.plusMonths(1))
                .duration(6)
                .maxPlaces(25)
                .status(InternshipProgramStatus.COMPLETED)
                .requirements(null)
                .goals(null)
                .competencies(null)
                .selectionStages(null)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existingProgramEntity));
        when(repository.save(any(InternshipProgramEntity.class))).thenReturn(existingProgramEntity);

        // When
        InternshipProgram result = internshipProgramService.updateInternshipProgram(command);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", existingProgramEntity.getTitle());
        assertEquals("Updated Description", existingProgramEntity.getDescription());
        assertEquals(6, existingProgramEntity.getDuration());
        assertEquals(25, existingProgramEntity.getMaxPlaces());
        assertEquals(InternshipProgramStatus.COMPLETED, existingProgramEntity.getStatus());
        verify(repository).findById(1L);
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldUpdateInternshipProgramWithPartialFields() throws JsonProcessingException {
        // Given
        UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                .id(1L)
                .title("Only Title Updated")
                .description(null)
                .startDate(null)
                .duration(null)
                .maxPlaces(null)
                .status(null)
                .requirements(null)
                .goals(null)
                .competencies(null)
                .selectionStages(null)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existingProgramEntity));
        when(repository.save(any(InternshipProgramEntity.class))).thenReturn(existingProgramEntity);

        // When
        InternshipProgram result = internshipProgramService.updateInternshipProgram(command);

        // Then
        assertNotNull(result);
        assertEquals("Only Title Updated", existingProgramEntity.getTitle());
        assertEquals("Learn Spring Boot", existingProgramEntity.getDescription()); // Should remain unchanged
        verify(repository).findById(1L);
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldUpdateInternshipProgramWithJsonFields() throws JsonProcessingException {
        // Given
        List<String> requirements = List.of("Updated Requirement");
        UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                .id(1L)
                .title(null)
                .description(null)
                .startDate(null)
                .duration(null)
                .maxPlaces(null)
                .status(null)
                .requirements(requirements)
                .goals(null)
                .competencies(null)
                .selectionStages(null)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existingProgramEntity));
        when(objectMapper.writeValueAsString(requirements)).thenReturn("[\"Updated Requirement\"]");
        when(repository.save(any(InternshipProgramEntity.class))).thenReturn(existingProgramEntity);

        // When
        InternshipProgram result = internshipProgramService.updateInternshipProgram(command);

        // Then
        assertNotNull(result);
        assertEquals("[\"Updated Requirement\"]", existingProgramEntity.getRequirements());
        verify(repository).findById(1L);
        verify(objectMapper).writeValueAsString(requirements);
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProgram() {
        // Given
        UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                .id(999L)
                .title("Updated Title")
                .build();

        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class,
                     () -> internshipProgramService.updateInternshipProgram(command));
        verify(repository).findById(999L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenJsonSerializationFailsDuringUpdate() throws JsonProcessingException {
        // Given
        List<String> requirements = List.of("Java");
        UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                .id(1L)
                .title(null)
                .description(null)
                .startDate(null)
                .duration(null)
                .maxPlaces(null)
                .status(null)
                .requirements(requirements)
                .goals(null)
                .competencies(null)
                .selectionStages(null)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existingProgramEntity));
        when(objectMapper.writeValueAsString(requirements))
                .thenThrow(new JsonProcessingException("JSON error") {
                });

        // When & Then
        assertThrows(IllegalArgumentException.class,
                     () -> internshipProgramService.updateInternshipProgram(command));
        verify(repository).findById(1L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteInternshipProgramSuccessfully() {
        // Given
        DeleteInternshipProgramCommand command = new DeleteInternshipProgramCommand(1L);

        when(repository.existsById(1L)).thenReturn(true);

        // When
        internshipProgramService.deleteInternshipProgram(command);

        // Then
        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentProgram() {
        // Given
        DeleteInternshipProgramCommand command = new DeleteInternshipProgramCommand(999L);

        when(repository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(NoSuchElementException.class,
                     () -> internshipProgramService.deleteInternshipProgram(command));
        verify(repository).existsById(999L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void shouldGetInternshipProgramByIdSuccessfully() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(existingProgramEntity));

        // When
        InternshipProgram result = internshipProgramService.getInternshipProgramById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Spring Boot Internship", result.getTitle());
        assertEquals("Learn Spring Boot", result.getDescription());
        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentProgram() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class,
                     () -> internshipProgramService.getInternshipProgramById(999L));
        verify(repository).findById(999L);
    }
}
