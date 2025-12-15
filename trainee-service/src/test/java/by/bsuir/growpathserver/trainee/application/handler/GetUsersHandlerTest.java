package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import by.bsuir.growpathserver.trainee.application.query.GetUsersQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GetUsersHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUsersHandler getUsersHandler;

    private List<UserEntity> testUsers;

    @BeforeEach
    void setUp() {
        UserEntity user1 = new UserEntity();
        user1.setId("user-001");
        user1.setEmail("user1@example.com");
        user1.setName("User One");
        user1.setRole(UserRole.INTERN);
        user1.setStatus(UserStatus.ACTIVE);
        user1.setCreatedAt(LocalDateTime.now());

        UserEntity user2 = new UserEntity();
        user2.setId("user-002");
        user2.setEmail("user2@example.com");
        user2.setName("User Two");
        user2.setRole(UserRole.MENTOR);
        user2.setStatus(UserStatus.ACTIVE);
        user2.setCreatedAt(LocalDateTime.now());

        testUsers = List.of(user1, user2);
    }

    @Test
    void shouldGetUsersWithDefaultPagination() {
        // Given
        GetUsersQuery query = GetUsersQuery.builder().build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> userPage = new PageImpl<>(testUsers, pageable, 2);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // When
        Page<User> result = getUsersHandler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldGetUsersWithCustomPagination() {
        // Given
        GetUsersQuery query = GetUsersQuery.builder()
                .page(2)
                .limit(5)
                .build();
        Pageable pageable = PageRequest.of(1, 5);
        Page<UserEntity> userPage = new PageImpl<>(testUsers, pageable, 2);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // When
        Page<User> result = getUsersHandler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getNumber());
        assertEquals(5, result.getSize());
    }

    @Test
    void shouldFilterUsersByRole() {
        // Given
        GetUsersQuery query = GetUsersQuery.builder()
                .role(UserRole.INTERN)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> userPage = new PageImpl<>(List.of(testUsers.get(0)), pageable, 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // When
        Page<User> result = getUsersHandler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(UserRole.INTERN, result.getContent().get(0).getRole());
    }

    @Test
    void shouldFilterUsersByStatus() {
        // Given
        GetUsersQuery query = GetUsersQuery.builder()
                .status(UserStatus.ACTIVE)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> userPage = new PageImpl<>(testUsers, pageable, 2);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // When
        Page<User> result = getUsersHandler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldSearchUsersByName() {
        // Given
        GetUsersQuery query = GetUsersQuery.builder()
                .search("One")
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> userPage = new PageImpl<>(List.of(testUsers.get(0)), pageable, 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // When
        Page<User> result = getUsersHandler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}

