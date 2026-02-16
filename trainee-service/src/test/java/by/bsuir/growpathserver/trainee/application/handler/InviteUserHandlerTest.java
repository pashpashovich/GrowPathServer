package by.bsuir.growpathserver.trainee.application.handler;

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
import org.springframework.kafka.core.KafkaTemplate;

import by.bsuir.growpathserver.trainee.application.command.InviteUserCommand;
import by.bsuir.growpathserver.trainee.application.service.RegistrationTokenService;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class InviteUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private RegistrationTokenService registrationTokenService;

    @InjectMocks
    private InviteUserHandler inviteUserHandler;

    private UserEntity pendingUser;

    @BeforeEach
    void setUp() {
        pendingUser = new UserEntity();
        pendingUser.setId(1L);
        pendingUser.setEmail("user@example.com");
        pendingUser.setName("Test User");
        pendingUser.setRole(UserRole.INTERN);
        pendingUser.setStatus(UserStatus.PENDING);
        pendingUser.setCreatedAt(LocalDateTime.now());
        pendingUser.setInvitationSentAt(null);
    }

    @Test
    void shouldInviteUserSuccessfully() {
        // Given
        InviteUserCommand command = new InviteUserCommand(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return entity;
        });
        when(registrationTokenService.createToken(1L)).thenReturn("test-token");

        // When
        User result = inviteUserHandler.handle(command);

        // Then
        assertNotNull(result);
        assertNotNull(result.getInvitationSentAt());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        InviteUserCommand command = new InviteUserCommand(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> inviteUserHandler.handle(command));
        verify(userRepository, never()).save(any());
    }
}

