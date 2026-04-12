package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.DeleteUserCommand;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DeleteUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdentityProviderPort identityProviderPort;

    @InjectMocks
    private DeleteUserHandler deleteUserHandler;

    @Test
    void shouldDeleteUserSuccessfully() {
        DeleteUserCommand command = new DeleteUserCommand(1L);
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setEmail("u@example.com");
        entity.setKeycloakUserId("kc-uuid-1");
        entity.setRole(UserRole.INTERN);
        entity.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        deleteUserHandler.handle(command);

        verify(identityProviderPort).deleteUser("kc-uuid-1", "u@example.com");
        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        DeleteUserCommand command = new DeleteUserCommand(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> deleteUserHandler.handle(command));
        verify(identityProviderPort, never()).deleteUser(any(), any());
        verify(userRepository, never()).deleteById(any());
    }
}
