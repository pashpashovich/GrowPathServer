package by.bsuir.growpathserver.trainee.infrastructure.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.dto.model.ChangeRoleRequest;
import by.bsuir.growpathserver.dto.model.CreateUserRequest;
import by.bsuir.growpathserver.dto.model.UpdateUserRequest;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IdentityProviderPort identityProviderPort;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        when(identityProviderPort.createUser(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("00000000-0000-0000-0000-000000000099");

        userRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.INTERN);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.saveAndFlush(testUser);
    }

    @Test
    void shouldGetUsersSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testUser.getId()))
                .andExpect(jsonPath("$.data[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.pagination").exists());
    }

    @Test
    void shouldGetUsersWithPagination() throws Exception {
        // Given - create more users
        for (int i = 2; i <= 5; i++) {
            UserEntity user = new UserEntity();
            user.setEmail("user" + i + "@example.com");
            user.setFirstName("User");
            user.setLastName(String.valueOf(i));
            user.setRole(UserRole.INTERN);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.saveAndFlush(user);
        }

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                                .param("page", "1")
                                .param("limit", "2")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.limit").value(2));
    }

    @Test
    void shouldFilterUsersByRole() throws Exception {
        // Given
        UserEntity mentor = new UserEntity();
        mentor.setEmail("mentor@example.com");
        mentor.setFirstName("Mentor");
        mentor.setLastName("User");
        mentor.setRole(UserRole.MENTOR);
        mentor.setStatus(UserStatus.ACTIVE);
        mentor.setCreatedAt(LocalDateTime.now());
        userRepository.saveAndFlush(mentor);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                                .param("role", "mentor")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].role").value("mentor"));
    }

    @Test
    void shouldFilterUsersByStatus() throws Exception {
        // Given
        UserEntity blockedUser = new UserEntity();
        blockedUser.setEmail("blocked@example.com");
        blockedUser.setFirstName("Blocked");
        blockedUser.setLastName("User");
        blockedUser.setRole(UserRole.INTERN);
        blockedUser.setStatus(UserStatus.BLOCKED);
        blockedUser.setCreatedAt(LocalDateTime.now());
        userRepository.saveAndFlush(blockedUser);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                                .param("status", "BLOCKED")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("blocked"));
    }

    @Test
    void shouldSearchUsersByName() throws Exception {
        // Given
        UserEntity user2 = new UserEntity();
        user2.setEmail("john@example.com");
        user2.setFirstName("John");
        user2.setLastName("Doe");
        user2.setRole(UserRole.INTERN);
        user2.setStatus(UserStatus.ACTIVE);
        user2.setCreatedAt(LocalDateTime.now());
        userRepository.saveAndFlush(user2);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                                .param("search", "John")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].firstName").value("John"))
                .andExpect(jsonPath("$.data[0].lastName").value("Doe"));
    }

    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", testUser.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", 0L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("newuser@example.com");
        request.setFirstName("New");
        request.setLastName("User");
        request.setRole(CreateUserRequest.RoleEnum.INTERN);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("intern"));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setEmail("updated@example.com");
        request.setRole(UpdateUserRequest.RoleEnum.MENTOR);

        // When & Then
        mockMvc.perform(put("/api/v1/users/{id}", testUser.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.role").value("mentor"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentUser() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");

        // When & Then
        mockMvc.perform(put("/api/v1/users/{id}", 0L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", testUser.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", 0L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockUserSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/users/{id}/block", testUser.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.status").value("blocked"));
    }

    @Test
    void shouldReturnNotFoundWhenBlockingNonExistentUser() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/users/{id}/block", 0L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUnblockUserSuccessfully() throws Exception {
        // Given - block user first
        testUser.setStatus(UserStatus.BLOCKED);
        testUser = userRepository.saveAndFlush(testUser);

        // When & Then
        mockMvc.perform(post("/api/v1/users/{id}/unblock", testUser.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.status").value("active"));
    }

    @Test
    void shouldChangeUserRoleSuccessfully() throws Exception {
        // Given
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setRole(ChangeRoleRequest.RoleEnum.MENTOR);

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/role", testUser.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.role").value("mentor"));
    }

    @Test
    void shouldInviteUserSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/users/{id}/invite", testUser.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.invitationSentAt").exists())
                .andExpect(jsonPath("$.message").value("Invitation sent successfully"));
    }

    @Test
    void shouldReturnNotFoundWhenInvitingNonExistentUser() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/users/{id}/invite", 0L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
