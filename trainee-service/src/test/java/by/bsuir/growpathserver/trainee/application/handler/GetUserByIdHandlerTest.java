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
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");
        testUser.setRole(UserRole.INTERN);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        // Given
        GetUserByIdQuery query = new GetUserByIdQuery(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        User result = getUserByIdHandler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("user@example.com", result.getEmail().value());
        assertEquals("Test User", result.getName());
        assertEquals(UserRole.INTERN, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        GetUserByIdQuery query = new GetUserByIdQuery(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> getUserByIdHandler.handle(query));
    }
}

