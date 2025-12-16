package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.query.GetInternshipProgramByIdQuery;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramService;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;

@ExtendWith(MockitoExtension.class)
class GetInternshipProgramByIdHandlerTest {

    @Mock
    private InternshipProgramService internshipProgramService;

    @InjectMocks
    private GetInternshipProgramByIdHandler handler;

    private GetInternshipProgramByIdQuery query;
    private InternshipProgram program;

    @BeforeEach
    void setUp() {
        query = new GetInternshipProgramByIdQuery(1L);

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
    void shouldGetInternshipProgramByIdSuccessfully() {
        // Given
        when(internshipProgramService.getInternshipProgramById(1L))
                .thenReturn(program);

        // When
        InternshipProgram result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Program", result.getTitle());
        verify(internshipProgramService).getInternshipProgramById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProgramNotFound() {
        // Given
        when(internshipProgramService.getInternshipProgramById(anyLong()))
                .thenThrow(new NoSuchElementException("Program not found"));

        // When & Then
        assertThrows(NoSuchElementException.class, () -> handler.handle(query));
        verify(internshipProgramService).getInternshipProgramById(1L);
    }
}
