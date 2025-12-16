package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramService;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;

@ExtendWith(MockitoExtension.class)
class CreateInternshipProgramHandlerTest {

    @Mock
    private InternshipProgramService internshipProgramService;

    @InjectMocks
    private CreateInternshipProgramHandler handler;

    private CreateInternshipProgramCommand command;
    private InternshipProgram program;

    @BeforeEach
    void setUp() {
        List<CreateInternshipProgramCommand.ProgramGoal> goals = new ArrayList<>();
        goals.add(new CreateInternshipProgramCommand.ProgramGoal("Learn Spring", "Master Spring Framework"));

        command = CreateInternshipProgramCommand.builder()
                .title("Test Program")
                .description("Test Description")
                .startDate(LocalDate.of(2024, 9, 1))
                .duration(6)
                .maxPlaces(20)
                .requirements(new ArrayList<>())
                .goals(goals)
                .competencies(new ArrayList<>())
                .selectionStages(new ArrayList<>())
                .status(InternshipProgramStatus.ACTIVE)
                .createdBy(1L)
                .build();

        InternshipProgramEntity entity = new InternshipProgramEntity();
        entity.setId(1L);
        entity.setTitle("Test Program");
        entity.setDescription("Test Description");
        entity.setStartDate(LocalDate.of(2024, 9, 1));
        entity.setDuration(6);
        entity.setMaxPlaces(20);
        entity.setStatus(InternshipProgramStatus.ACTIVE);
        entity.setCreatedBy(1L);

        program = InternshipProgram.fromEntity(entity);
    }

    @Test
    void shouldCreateInternshipProgramSuccessfully() {
        // Given
        when(internshipProgramService.createInternshipProgram(any(CreateInternshipProgramCommand.class)))
                .thenReturn(program);

        // When
        InternshipProgram result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals("Test Program", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(6, result.getDuration());
        assertEquals(20, result.getMaxPlaces());
        assertEquals(InternshipProgramStatus.ACTIVE, result.getStatus());
        verify(internshipProgramService).createInternshipProgram(command);
    }
}
