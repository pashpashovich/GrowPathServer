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

import by.bsuir.growpathserver.trainee.application.command.ChangeUserRoleCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ChangeUserRoleHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChangeUserRoleHandler changeUserRoleHandler;

    private UserEntity internUser;

    @BeforeEach
    void setUp() {
        internUser = new UserEntity();
        internUser.setId(1L);
        internUser.setEmail("user@example.com");
        internUser.setName("Test User");
        internUser.setRole(UserRole.INTERN);
        internUser.setStatus(UserStatus.ACTIVE);
        internUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldChangeUserRoleSuccessfully() {
        // Given
        ChangeUserRoleCommand command = ChangeUserRoleCommand.builder()
                .userId(1L)
                .role(UserRole.MENTOR)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(internUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return entity;
        });

        // When
        User result = changeUserRoleHandler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(UserRole.MENTOR, result.getRole());
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldChangeRoleFromInternToHr() {
        // Given
        ChangeUserRoleCommand command = ChangeUserRoleCommand.builder()
                .userId(1L)
                .role(UserRole.HR)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(internUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return entity;
        });

        // When
        User result = changeUserRoleHandler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(UserRole.HR, result.getRole());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        ChangeUserRoleCommand command = ChangeUserRoleCommand.builder()
                .userId(999L)
                .role(UserRole.MENTOR)
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> changeUserRoleHandler.handle(command));
        verify(userRepository, never()).save(any());
    }
}

