package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.DeleteInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramService;

@ExtendWith(MockitoExtension.class)
class DeleteInternshipProgramHandlerTest {

    @Mock
    private InternshipProgramService internshipProgramService;

    @InjectMocks
    private DeleteInternshipProgramHandler handler;

    private DeleteInternshipProgramCommand command;

    @BeforeEach
    void setUp() {
        command = new DeleteInternshipProgramCommand(1L);
    }

    @Test
    void shouldDeleteInternshipProgramSuccessfully() {
        // When
        handler.handle(command);

        // Then
        verify(internshipProgramService).deleteInternshipProgram(command);
    }

    @Test
    void shouldThrowExceptionWhenProgramNotFound() {
        // Given
        doThrow(new NoSuchElementException("Program not found"))
                .when(internshipProgramService).deleteInternshipProgram(any());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> handler.handle(command));
        verify(internshipProgramService).deleteInternshipProgram(command);
    }
}
