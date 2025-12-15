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

import by.bsuir.growpathserver.trainee.application.command.BlockUserCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BlockUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BlockUserHandler blockUserHandler;

    private UserEntity activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new UserEntity();
        activeUser.setId("user-001");
        activeUser.setEmail("user@example.com");
        activeUser.setName("Test User");
        activeUser.setRole(UserRole.INTERN);
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldBlockUserSuccessfully() {
        // Given
        BlockUserCommand command = new BlockUserCommand("user-001");

        when(userRepository.findById("user-001")).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return entity;
        });

        // When
        User result = blockUserHandler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(UserStatus.BLOCKED, result.getStatus());
        assertEquals("user-001", result.getId());
        verify(userRepository).findById("user-001");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        BlockUserCommand command = new BlockUserCommand("non-existent");

        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> blockUserHandler.handle(command));
        verify(userRepository, never()).save(any());
    }
}

