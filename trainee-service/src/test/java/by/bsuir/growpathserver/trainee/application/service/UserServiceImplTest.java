package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import by.bsuir.growpathserver.trainee.application.command.CreateUserCommand;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import by.bsuir.growpathserver.trainee.application.service.impl.UserServiceImpl;
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

    @Mock
    private IdentityProviderPort identityProviderPort;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUserSuccessfully() {
        CreateUserCommand command = CreateUserCommand.builder()
                .email("newuser@example.com")
                .firstName("New")
                .lastName("User")
                .patronymicName(null)
                .role(UserRole.INTERN)
                .invitedBy(1L)
                .build();

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        UserEntity[] holder = new UserEntity[1];
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(1L);
            }
            holder[0] = entity;
            return entity;
        });
        when(userRepository.findById(1L)).thenAnswer(invocation -> Optional.ofNullable(holder[0]));
        when(identityProviderPort.createUser(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("11111111-1111-1111-1111-111111111111");

        User result = userService.createUser(command);

        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail().value());
        assertEquals("New", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals(UserRole.INTERN, result.getRole());
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository, times(2)).save(any(UserEntity.class));
        verify(identityProviderPort).createUser("newuser@example.com", "New", "User", "INTERN");
        assertEquals("11111111-1111-1111-1111-111111111111", holder[0].getKeycloakUserId());

        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        UserCreatedEvent event = eventCaptor.getValue();
        assertNotNull(event);
        assertEquals("newuser@example.com", event.email());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        CreateUserCommand command = CreateUserCommand.builder()
                .email("existing@example.com")
                .firstName("New")
                .lastName("User")
                .patronymicName(null)
                .role(UserRole.INTERN)
                .invitedBy(1L)
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(command));
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
