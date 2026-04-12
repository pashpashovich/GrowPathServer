package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        existingUser.setFirstName("Old");
        existingUser.setLastName("Name");
        existingUser.setPatronymicName("Patronymic");
        existingUser.setRole(UserRole.INTERN);
        existingUser.setStatus(UserStatus.ACTIVE);
        existingUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        UpdateUserCommand command = new UpdateUserCommand(
                1L,
                Optional.of("new@example.com"),
                Optional.of("Ivan"),
                Optional.of("Petrov"),
                Optional.empty(),
                Optional.of(UserRole.MENTOR)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = updateUserHandler.handle(command);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail().value());
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Petrov", result.getLastName());
        assertEquals(UserRole.MENTOR, result.getRole());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldUpdateOnlyNameWhenOtherOptionalsEmpty() {
        UpdateUserCommand command = new UpdateUserCommand(
                1L,
                Optional.empty(),
                Optional.of("Updated"),
                Optional.of("Surname"),
                Optional.empty(),
                Optional.empty()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = updateUserHandler.handle(command);

        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Surname", result.getLastName());
        assertEquals("old@example.com", result.getEmail().value());
        assertEquals(UserRole.INTERN, result.getRole());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void shouldClearPatronymicWhenBlankString() {
        UpdateUserCommand command = new UpdateUserCommand(
                1L,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("   "),
                Optional.empty()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = updateUserHandler.handle(command);

        assertNull(result.getPatronymicName());
    }

    @Test
    void shouldNotUpdateEmailIfSameAsCurrent() {
        UpdateUserCommand command = new UpdateUserCommand(
                1L,
                Optional.of("old@example.com"),
                Optional.of("Ivan"),
                Optional.of("Petrov"),
                Optional.empty(),
                Optional.empty()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = updateUserHandler.handle(command);

        assertEquals("old@example.com", result.getEmail().value());
        assertEquals("Ivan", result.getFirstName());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void shouldThrowWhenOnlyFirstNameProvided() {
        UpdateUserCommand command = new UpdateUserCommand(
                1L,
                Optional.empty(),
                Optional.of("Ivan"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalArgumentException.class, () -> updateUserHandler.handle(command));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UpdateUserCommand command = new UpdateUserCommand(
                999L,
                Optional.empty(),
                Optional.of("X"),
                Optional.of("Y"),
                Optional.empty(),
                Optional.empty()
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> updateUserHandler.handle(command));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UpdateUserCommand command = new UpdateUserCommand(
                1L,
                Optional.of("existing@example.com"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> updateUserHandler.handle(command));
        verify(userRepository, never()).save(any());
    }
}
