package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.DeleteUserCommand;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DeleteUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserHandler deleteUserHandler;

    @Test
    void shouldDeleteUserSuccessfully() {
        // Given
        DeleteUserCommand command = new DeleteUserCommand(1L);

        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        deleteUserHandler.handle(command);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        DeleteUserCommand command = new DeleteUserCommand(999L);

        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> deleteUserHandler.handle(command));
        verify(userRepository, never()).deleteById(any());
    }
}

