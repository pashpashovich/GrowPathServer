package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramService;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;

@ExtendWith(MockitoExtension.class)
class UpdateInternshipProgramHandlerTest {

    @Mock
    private InternshipProgramService internshipProgramService;

    @InjectMocks
    private UpdateInternshipProgramHandler handler;

    private UpdateInternshipProgramCommand command;
    private InternshipProgram program;

    @BeforeEach
    void setUp() {
        command = UpdateInternshipProgramCommand.builder()
                .id(1L)
                .title("Updated Program")
                .description("Updated Description")
                .duration(8)
                .maxPlaces(25)
                .status(InternshipProgramStatus.COMPLETED)
                .build();

        InternshipProgramEntity entity = new InternshipProgramEntity();
        entity.setId(1L);
        entity.setTitle("Updated Program");
        entity.setDescription("Updated Description");
        entity.setStartDate(LocalDate.of(2024, 9, 1));
        entity.setDuration(8);
        entity.setMaxPlaces(25);
        entity.setStatus(InternshipProgramStatus.COMPLETED);
        entity.setCreatedBy(1L);

        program = InternshipProgram.fromEntity(entity);
    }

    @Test
    void shouldUpdateInternshipProgramSuccessfully() {
        // Given
        when(internshipProgramService.updateInternshipProgram(any(UpdateInternshipProgramCommand.class)))
                .thenReturn(program);

        // When
        InternshipProgram result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Program", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(8, result.getDuration());
        assertEquals(25, result.getMaxPlaces());
        assertEquals(InternshipProgramStatus.COMPLETED, result.getStatus());
        verify(internshipProgramService).updateInternshipProgram(command);
    }
}
