package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import by.bsuir.growpathserver.trainee.application.command.CreateUserCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.events.UserCreatedEvent;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        CreateUserCommand command = CreateUserCommand.builder()
                .email("newuser@example.com")
                .name("New User")
                .role(UserRole.INTERN)
                .invitedBy(1L)
                .build();

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // When
        User result = userService.createUser(command);

        // Then
        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail().value());
        assertEquals("New User", result.getName());
        assertEquals(UserRole.INTERN, result.getRole());
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository).save(any(UserEntity.class));

        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        UserCreatedEvent event = eventCaptor.getValue();
        assertNotNull(event);
        assertEquals("newuser@example.com", event.email());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        CreateUserCommand command = CreateUserCommand.builder()
                .email("existing@example.com")
                .name("New User")
                .role(UserRole.INTERN)
                .invitedBy(1L)
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(command));
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
