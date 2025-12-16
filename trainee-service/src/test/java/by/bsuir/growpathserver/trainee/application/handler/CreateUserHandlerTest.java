package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.CreateUserCommand;
import by.bsuir.growpathserver.trainee.application.service.UserService;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;

@ExtendWith(MockitoExtension.class)
class CreateUserHandlerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CreateUserHandler createUserHandler;

    @Test
    void shouldDelegateToUserService() {
        // Given
        CreateUserCommand command = CreateUserCommand.builder()
                .email("user@example.com")
                .name("Test User")
                .role(UserRole.INTERN)
                .invitedBy(1L)
                .build();

        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setEmail("user@example.com");
        entity.setName("Test User");
        entity.setRole(UserRole.INTERN);

        User expectedUser = User.fromEntity(entity);

        when(userService.createUser(command)).thenReturn(expectedUser);

        // When
        User result = createUserHandler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("user@example.com", result.getEmail().value());
        verify(userService).createUser(command);
    }
}

