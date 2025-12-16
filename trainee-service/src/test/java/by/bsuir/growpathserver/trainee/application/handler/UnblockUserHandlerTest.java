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

import by.bsuir.growpathserver.trainee.application.command.UnblockUserCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UnblockUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UnblockUserHandler unblockUserHandler;

    private UserEntity blockedUser;

    @BeforeEach
    void setUp() {
        blockedUser = new UserEntity();
        blockedUser.setId(1L);
        blockedUser.setEmail("user@example.com");
        blockedUser.setName("Test User");
        blockedUser.setRole(UserRole.INTERN);
        blockedUser.setStatus(UserStatus.BLOCKED);
        blockedUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldUnblockUserSuccessfully() {
        // Given
        UnblockUserCommand command = new UnblockUserCommand(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(blockedUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return entity;
        });

        // When
        User result = unblockUserHandler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UnblockUserCommand command = new UnblockUserCommand(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> unblockUserHandler.handle(command));
        verify(userRepository, never()).save(any());
    }
}

