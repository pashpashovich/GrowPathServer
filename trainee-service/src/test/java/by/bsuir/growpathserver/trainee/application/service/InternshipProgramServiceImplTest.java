package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.service.impl.InternshipProgramServiceImpl;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramRequirementEntity;
import by.bsuir.growpathserver.trainee.domain.exception.DuplicateInternshipProgramTitleException;
import by.bsuir.growpathserver.trainee.domain.exception.InternshipProgramLockedException;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.InternshipProgramEntityMapperImpl;
import by.bsuir.growpathserver.trainee.infrastructure.repository.CompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;

@ExtendWith(MockitoExtension.class)
class InternshipProgramServiceImplTest {

    @Mock
    private InternshipProgramRepository repository;

    @Mock
    private CompetencyRepository competencyRepository;

    private InternshipProgramServiceImpl internshipProgramService;

    private InternshipProgramEntity existingProgramEntity;
    private LocalDate startDate;
    private CompetencyEntity competency;

    @BeforeEach
    void setUp() {
        internshipProgramService = new InternshipProgramServiceImpl(
                repository,
                competencyRepository,
                new InternshipProgramEntityMapperImpl());

        startDate = LocalDate.now().plusMonths(1);
        competency = new CompetencyEntity();
        competency.setId(10L);
        competency.setName("Java");

        existingProgramEntity = new InternshipProgramEntity();
        existingProgramEntity.setId(1L);
        existingProgramEntity.setTitle("Spring Boot Internship");
        existingProgramEntity.setDescription("Learn Spring Boot");
        existingProgramEntity.setStartDate(startDate);
        existingProgramEntity.setDuration(3);
        existingProgramEntity.setMaxPlaces(20);
        existingProgramEntity.setStatus(InternshipProgramStatus.ACTIVE);
        addRequirement(existingProgramEntity, "Java");
        addRequirement(existingProgramEntity, "Spring");
        existingProgramEntity.getCompetencies().add(competency);
    }

    private static void addRequirement(InternshipProgramEntity program, String text) {
        InternshipProgramRequirementEntity row = new InternshipProgramRequirementEntity();
        row.setInternshipProgram(program);
        row.setRequirementText(text);
        program.getRequirementItems().add(row);
    }

    @Test
    void shouldCreateInternshipProgramSuccessfully() {
        List<String> requirements = List.of("Java", "Spring Boot");
        CreateInternshipProgramCommand.ProgramGoal goal = new CreateInternshipProgramCommand.ProgramGoal(
                "Learn Spring", "Master Spring Framework");
        List<CreateInternshipProgramCommand.ProgramGoal> goals = List.of(goal);
        CreateInternshipProgramCommand.SelectionStage stage = new CreateInternshipProgramCommand.SelectionStage(
                "Interview", "Technical interview", 1);
        List<CreateInternshipProgramCommand.SelectionStage> stages = List.of(stage);

        CreateInternshipProgramCommand command = CreateInternshipProgramCommand.builder()
                .title("New Internship")
                .description("New Description")
                .startDate(startDate)
                .duration(6)
                .maxPlaces(15)
                .itDirection("BACKEND")
                .competencyIds(List.of(10L))
                .requirements(requirements)
                .goals(goals)
                .selectionStages(stages)
                .status(InternshipProgramStatus.ACTIVE)
                .createdBy(1L)
                .build();

        when(repository.existsByTitleIgnoreCase("New Internship")).thenReturn(false);
        when(competencyRepository.countByIdIn(List.of(10L))).thenReturn(1L);
        when(competencyRepository.findAllById(List.of(10L))).thenReturn(List.of(competency));
        when(repository.save(any(InternshipProgramEntity.class))).thenAnswer(invocation -> {
            InternshipProgramEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        InternshipProgram result = internshipProgramService.createInternshipProgram(command);

        assertNotNull(result);
        assertEquals("New Internship", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(6, result.getDuration());
        assertEquals(15, result.getMaxPlaces());
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldCreateInternshipProgramWithNullJsonFields() {
        CreateInternshipProgramCommand command = CreateInternshipProgramCommand.builder()
                .title("Simple Internship")
                .description("Simple Description")
                .startDate(startDate)
                .duration(3)
                .maxPlaces(10)
                .itDirection(null)
                .competencyIds(null)
                .requirements(null)
                .goals(null)
                .selectionStages(null)
                .status(InternshipProgramStatus.ACTIVE)
                .createdBy(1L)
                .build();

        when(repository.existsByTitleIgnoreCase("Simple Internship")).thenReturn(false);
        when(repository.save(any(InternshipProgramEntity.class))).thenAnswer(invocation -> {
            InternshipProgramEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        InternshipProgram result = internshipProgramService.createInternshipProgram(command);

        assertNotNull(result);
        assertEquals("Simple Internship", result.getTitle());
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldThrowWhenDuplicateTitleOnCreate() {
        CreateInternshipProgramCommand command = CreateInternshipProgramCommand.builder()
                .title("Dup")
                .description("D")
                .startDate(startDate)
                .duration(3)
                .maxPlaces(10)
                .competencyIds(null)
                .requirements(null)
                .goals(null)
                .selectionStages(null)
                .status(InternshipProgramStatus.ACTIVE)
                .createdBy(1L)
                .build();

        when(repository.existsByTitleIgnoreCase("Dup")).thenReturn(true);

        assertThrows(DuplicateInternshipProgramTitleException.class,
                () -> internshipProgramService.createInternshipProgram(command));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateInternshipProgramSuccessfully() {
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
                .competencyIds(null)
                .selectionStages(null)
                .build();

        when(repository.findWithCollectionsById(1L)).thenReturn(Optional.of(existingProgramEntity));
        when(repository.save(any(InternshipProgramEntity.class))).thenReturn(existingProgramEntity);

        InternshipProgram result = internshipProgramService.updateInternshipProgram(command);

        assertNotNull(result);
        assertEquals("Updated Title", existingProgramEntity.getTitle());
        assertEquals("Updated Description", existingProgramEntity.getDescription());
        assertEquals(6, existingProgramEntity.getDuration());
        assertEquals(25, existingProgramEntity.getMaxPlaces());
        assertEquals(InternshipProgramStatus.COMPLETED, existingProgramEntity.getStatus());
        verify(repository).findWithCollectionsById(1L);
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldUpdateInternshipProgramWithPartialFields() {
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
                .competencyIds(null)
                .selectionStages(null)
                .build();

        when(repository.findWithCollectionsById(1L)).thenReturn(Optional.of(existingProgramEntity));
        when(repository.save(any(InternshipProgramEntity.class))).thenReturn(existingProgramEntity);

        InternshipProgram result = internshipProgramService.updateInternshipProgram(command);

        assertNotNull(result);
        assertEquals("Only Title Updated", existingProgramEntity.getTitle());
        assertEquals("Learn Spring Boot", existingProgramEntity.getDescription());
        verify(repository).findWithCollectionsById(1L);
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldUpdateInternshipProgramWithStructuredRequirements() {
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
                .competencyIds(null)
                .selectionStages(null)
                .build();

        when(repository.findWithCollectionsById(1L)).thenReturn(Optional.of(existingProgramEntity));
        when(repository.save(any(InternshipProgramEntity.class))).thenReturn(existingProgramEntity);

        InternshipProgram result = internshipProgramService.updateInternshipProgram(command);

        assertNotNull(result);
        List<String> texts = existingProgramEntity.getRequirementItems().stream()
                .sorted(Comparator.comparingLong(InternshipProgramRequirementEntity::getId))
                .map(InternshipProgramRequirementEntity::getRequirementText)
                .toList();
        assertEquals(List.of("Updated Requirement"), texts);
        verify(repository).findWithCollectionsById(1L);
        verify(repository).save(any(InternshipProgramEntity.class));
    }

    @Test
    void shouldThrowWhenStartedProgramStructuralUpdate() {
        existingProgramEntity.setStartDate(LocalDate.now().minusDays(1));
        UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                .id(1L)
                .title("Changed")
                .build();

        when(repository.findWithCollectionsById(1L)).thenReturn(Optional.of(existingProgramEntity));

        assertThrows(InternshipProgramLockedException.class,
                () -> internshipProgramService.updateInternshipProgram(command));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldAllowArchiveWhenStarted() {
        existingProgramEntity.setStartDate(LocalDate.now().minusDays(1));
        UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                .id(1L)
                .status(InternshipProgramStatus.ARCHIVED)
                .build();

        when(repository.findWithCollectionsById(1L)).thenReturn(Optional.of(existingProgramEntity));
        when(repository.save(any(InternshipProgramEntity.class))).thenReturn(existingProgramEntity);

        internshipProgramService.updateInternshipProgram(command);

        assertEquals(InternshipProgramStatus.ARCHIVED, existingProgramEntity.getStatus());
        verify(repository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProgram() {
        UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                .id(999L)
                .title("Updated Title")
                .build();

        when(repository.findWithCollectionsById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> internshipProgramService.updateInternshipProgram(command));
        verify(repository).findWithCollectionsById(999L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteInternshipProgramSuccessfully() {
        DeleteInternshipProgramCommand command = new DeleteInternshipProgramCommand(1L);

        when(repository.existsById(1L)).thenReturn(true);

        internshipProgramService.deleteInternshipProgram(command);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentProgram() {
        DeleteInternshipProgramCommand command = new DeleteInternshipProgramCommand(999L);

        when(repository.existsById(999L)).thenReturn(false);

        assertThrows(NoSuchElementException.class,
                () -> internshipProgramService.deleteInternshipProgram(command));
        verify(repository).existsById(999L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void shouldGetInternshipProgramByIdSuccessfully() {
        when(repository.findWithCollectionsById(1L)).thenReturn(Optional.of(existingProgramEntity));

        InternshipProgram result = internshipProgramService.getInternshipProgramById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Spring Boot Internship", result.getTitle());
        assertEquals("Learn Spring Boot", result.getDescription());
        verify(repository).findWithCollectionsById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentProgram() {
        when(repository.findWithCollectionsById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> internshipProgramService.getInternshipProgramById(999L));
        verify(repository).findWithCollectionsById(999L);
    }
}
