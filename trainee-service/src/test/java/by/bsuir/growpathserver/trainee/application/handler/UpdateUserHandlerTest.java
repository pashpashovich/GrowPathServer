package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.UpdateUserCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UpdateUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserHandler updateUserHandler;

    private UserEntity existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setName("Old Name");
        existingUser.setRole(UserRole.INTERN);
        existingUser.setStatus(UserStatus.ACTIVE);
        existingUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        // Given
        UpdateUserCommand command = UpdateUserCommand.builder()
                .userId(1L)
                .email("new@example.com")
                .name("New Name")
                .role(UserRole.MENTOR)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return entity;
        });

        // When
        User result = updateUserHandler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail().value());
        assertEquals("New Name", result.getName());
        assertEquals(UserRole.MENTOR, result.getRole());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldUpdateOnlyNameWhenOtherFieldsAreNull() {
        // Given
        UpdateUserCommand command = UpdateUserCommand.builder()
                .userId(1L)
                .name("Updated Name")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return entity;
        });

        // When
        User result = updateUserHandler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("old@example.com", result.getEmail().value());
        assertEquals(UserRole.INTERN, result.getRole());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void shouldNotUpdateEmailIfSameAsCurrent() {
        // Given
        UpdateUserCommand command = UpdateUserCommand.builder()
                .userId(1L)
                .email("old@example.com")
                .name("New Name")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return entity;
        });

        // When
        User result = updateUserHandler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals("old@example.com", result.getEmail().value());
        assertEquals("New Name", result.getName());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UpdateUserCommand command = UpdateUserCommand.builder()
                .userId(999L)
                .name("New Name")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> updateUserHandler.handle(command));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        UpdateUserCommand command = UpdateUserCommand.builder()
                .userId(1L)
                .email("existing@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> updateUserHandler.handle(command));
        verify(userRepository, never()).save(any());
    }
}

