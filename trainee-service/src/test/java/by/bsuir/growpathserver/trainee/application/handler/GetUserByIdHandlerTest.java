package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import by.bsuir.growpathserver.trainee.application.query.GetUserByIdQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GetUserByIdHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserByIdHandler getUserByIdHandler;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("user-001");
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");
        testUser.setRole(UserRole.INTERN);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        // Given
        GetUserByIdQuery query = new GetUserByIdQuery("user-001");

        when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));

        // When
        User result = getUserByIdHandler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals("user-001", result.getId());
        assertEquals("user@example.com", result.getEmail().value());
        assertEquals("Test User", result.getName());
        assertEquals(UserRole.INTERN, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(userRepository).findById("user-001");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        GetUserByIdQuery query = new GetUserByIdQuery("non-existent");

        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> getUserByIdHandler.handle(query));
    }
}

