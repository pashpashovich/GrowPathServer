package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.trainee.dto.model.users.CreateUserRequest;
import by.bsuir.growpathserver.trainee.dto.model.users.PaginationResponse;
import by.bsuir.growpathserver.trainee.dto.model.users.UserListResponse;
import by.bsuir.growpathserver.trainee.dto.model.users.UserResponse;
import by.bsuir.growpathserver.trainee.application.command.CreateUserCommand;
import by.bsuir.growpathserver.trainee.application.handler.CreateUserHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetUserByIdHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetUsersHandler;
import by.bsuir.growpathserver.trainee.application.query.GetUserByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUsersQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final CreateUserHandler createUserHandler;
    private final GetUsersHandler getUsersHandler;
    private final GetUserByIdHandler getUserByIdHandler;
    private final UserMapper userMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        try {
            GetUsersQuery query = GetUsersQuery.builder()
                    .page(page)
                    .limit(limit)
                    .role(role != null ? UserRole.fromString(role) : null)
                    .status(status != null ? UserStatus.fromString(status) : null)
                    .search(search)
                    .build();

            Page<User> usersPage = getUsersHandler.handle(query);

            UserListResponse response = new UserListResponse();
            response.setData(usersPage.getContent().stream()
                                     .map(userMapper::toUserResponse)
                                     .toList());

            PaginationResponse pagination = new PaginationResponse();
            pagination.setPage(usersPage.getNumber() + 1);
            pagination.setLimit(usersPage.getSize());
            pagination.setTotal((int) usersPage.getTotalElements());
            pagination.setTotalPages(usersPage.getTotalPages());
            response.setPagination(pagination);

            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            GetUserByIdQuery query = new GetUserByIdQuery(id);
            User user = getUserByIdHandler.handle(query);
            UserResponse response = userMapper.toUserResponse(user);
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<?> createUser(
            @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            if (request.getRole() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", "Role is required"));
            }
            CreateUserCommand command = CreateUserCommand.builder()
                    .email(request.getEmail())
                    .name(request.getName())
                    .role(UserRole.fromString(request.getRole().getValue()))
                    .invitedBy(jwt.getSubject())
                    .build();

            User user = createUserHandler.handle(command);
            UserResponse response = userMapper.toUserResponse(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }
}
