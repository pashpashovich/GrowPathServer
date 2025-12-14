package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.UsersApi;
import by.bsuir.growpathserver.dto.model.ChangeRoleRequest;
import by.bsuir.growpathserver.dto.model.CreateUserRequest;
import by.bsuir.growpathserver.dto.model.InvitationResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.dto.model.UpdateUserRequest;
import by.bsuir.growpathserver.dto.model.UserListResponse;
import by.bsuir.growpathserver.dto.model.UserResponse;
import by.bsuir.growpathserver.dto.model.UserRoleResponse;
import by.bsuir.growpathserver.dto.model.UserStatusResponse;
import by.bsuir.growpathserver.trainee.application.command.BlockUserCommand;
import by.bsuir.growpathserver.trainee.application.command.ChangeUserRoleCommand;
import by.bsuir.growpathserver.trainee.application.command.CreateUserCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteUserCommand;
import by.bsuir.growpathserver.trainee.application.command.InviteUserCommand;
import by.bsuir.growpathserver.trainee.application.command.UnblockUserCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateUserCommand;
import by.bsuir.growpathserver.trainee.application.handler.BlockUserHandler;
import by.bsuir.growpathserver.trainee.application.handler.ChangeUserRoleHandler;
import by.bsuir.growpathserver.trainee.application.handler.CreateUserHandler;
import by.bsuir.growpathserver.trainee.application.handler.DeleteUserHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetUserByIdHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetUsersHandler;
import by.bsuir.growpathserver.trainee.application.handler.InviteUserHandler;
import by.bsuir.growpathserver.trainee.application.handler.UnblockUserHandler;
import by.bsuir.growpathserver.trainee.application.handler.UpdateUserHandler;
import by.bsuir.growpathserver.trainee.application.query.GetUserByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUsersQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    private final GetUsersHandler getUsersHandler;
    private final GetUserByIdHandler getUserByIdHandler;
    private final CreateUserHandler createUserHandler;
    private final UpdateUserHandler updateUserHandler;
    private final DeleteUserHandler deleteUserHandler;
    private final BlockUserHandler blockUserHandler;
    private final UnblockUserHandler unblockUserHandler;
    private final ChangeUserRoleHandler changeUserRoleHandler;
    private final InviteUserHandler inviteUserHandler;
    private final UserMapper userMapper;

    @Override
    public ResponseEntity<UserListResponse> getUsers(Integer page,
                                                     Integer limit,
                                                     String role,
                                                     String status,
                                                     String search) {
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
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(String id) {
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

    @Override
    public ResponseEntity<UserResponse> createUser(CreateUserRequest createUserRequest) {
        try {
            // TODO: Get invitedBy from JWT token when authentication is properly configured
            String invitedBy = "system"; // Should be extracted from JWT

            CreateUserCommand command = CreateUserCommand.builder()
                    .email(createUserRequest.getEmail())
                    .name(createUserRequest.getName())
                    .role(UserRole.fromString(createUserRequest.getRole().getValue()))
                    .invitedBy(invitedBy)
                    .build();

            User user = createUserHandler.handle(command);
            UserResponse response = userMapper.toUserResponse(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(String id, UpdateUserRequest updateUserRequest) {
        try {
            UpdateUserCommand command = UpdateUserCommand.builder()
                    .userId(id)
                    .email(updateUserRequest.getEmail())
                    .name(updateUserRequest.getName())
                    .role(updateUserRequest.getRole() != null ?
                                  UserRole.fromString(updateUserRequest.getRole().getValue()) : null)
                    .build();

            User user = updateUserHandler.handle(command);
            UserResponse response = userMapper.toUserResponse(user);
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<MessageResponse> deleteUser(String id) {
        try {
            DeleteUserCommand command = new DeleteUserCommand(id);
            deleteUserHandler.handle(command);

            MessageResponse response = new MessageResponse();
            response.setMessage("User deleted successfully");
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<UserStatusResponse> blockUser(String id) {
        try {
            BlockUserCommand command = new BlockUserCommand(id);
            User user = blockUserHandler.handle(command);

            UserStatusResponse response = new UserStatusResponse();
            response.setId(user.getId());
            response.setStatus(UserStatusResponse.StatusEnum.fromValue(user.getStatus().getValue()));
            response.setUpdatedAt(LocalDateTime.now());

            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<UserStatusResponse> unblockUser(String id) {
        try {
            UnblockUserCommand command = new UnblockUserCommand(id);
            User user = unblockUserHandler.handle(command);

            UserStatusResponse response = new UserStatusResponse();
            response.setId(user.getId());
            response.setStatus(UserStatusResponse.StatusEnum.fromValue(user.getStatus().getValue()));
            response.setUpdatedAt(LocalDateTime.now());

            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<UserRoleResponse> changeUserRole(String id, ChangeRoleRequest changeRoleRequest) {
        try {
            ChangeUserRoleCommand command = ChangeUserRoleCommand.builder()
                    .userId(id)
                    .role(UserRole.fromString(changeRoleRequest.getRole().getValue()))
                    .build();

            User user = changeUserRoleHandler.handle(command);

            UserRoleResponse response = new UserRoleResponse();
            response.setId(user.getId());
            response.setRole(UserRoleResponse.RoleEnum.fromValue(user.getRole().getValue()));
            response.setUpdatedAt(LocalDateTime.now());

            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<InvitationResponse> inviteUser(String id) {
        try {
            InviteUserCommand command = new InviteUserCommand(id);
            User user = inviteUserHandler.handle(command);

            InvitationResponse response = new InvitationResponse();
            response.setId(user.getId());
            if (user.getInvitationSentAt() != null) {
                response.setInvitationSentAt(user.getInvitationSentAt());
            }
            response.setStatus(InvitationResponse.StatusEnum.PENDING);
            response.setMessage("Invitation sent successfully");

            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
