package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.query.GetUserByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUsersQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsersApplicationFacade {

    private static final String MSG_USER_DELETED = "User deleted successfully";
    private static final String MSG_INVITATION_SENT = "Invitation sent successfully";

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
    private final CurrentApplicationUserResolver currentApplicationUserResolver;

    public UserListResponse listUsers(Integer page, Integer limit, String role, String status, String search) {
        GetUsersQuery query = GetUsersQuery.builder()
                .page(page)
                .limit(limit)
                .role(Objects.nonNull(role) ? UserRole.fromString(role) : null)
                .status(Objects.nonNull(status) ? UserStatus.fromString(status) : null)
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

        return response;
    }

    public UserResponse getUserById(String id) {
        long userId = parseUserId(id);
        GetUserByIdQuery query = new GetUserByIdQuery(userId);
        User user = getUserByIdHandler.handle(query);
        return userMapper.toUserResponse(user);
    }

    public UserResponse createUser(CreateUserRequest createUserRequest) {
        Long invitedBy = currentApplicationUserResolver.resolveCurrentUserDatabaseId().orElse(null);

        CreateUserCommand command = CreateUserCommand.builder()
                .email(createUserRequest.getEmail())
                .firstName(createUserRequest.getFirstName())
                .lastName(createUserRequest.getLastName())
                .patronymicName(createUserRequest.getPatronymicName())
                .role(UserRole.fromString(createUserRequest.getRole().getValue()))
                .invitedBy(invitedBy)
                .build();

        User user = createUserHandler.handle(command);
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(String id, UpdateUserRequest updateUserRequest) {
        long userId = parseUserId(id);
        UpdateUserCommand command = new UpdateUserCommand(
                userId,
                Optional.ofNullable(updateUserRequest.getEmail()),
                Optional.ofNullable(updateUserRequest.getFirstName()),
                Optional.ofNullable(updateUserRequest.getLastName()),
                Optional.ofNullable(updateUserRequest.getPatronymicName()),
                Optional.ofNullable(updateUserRequest.getRole())
                        .map(r -> UserRole.fromString(r.getValue()))
        );

        User user = updateUserHandler.handle(command);
        return userMapper.toUserResponse(user);
    }

    public MessageResponse deleteUser(String id) {
        long userId = parseUserId(id);
        deleteUserHandler.handle(new DeleteUserCommand(userId));
        MessageResponse response = new MessageResponse();
        response.setMessage(MSG_USER_DELETED);
        return response;
    }

    public UserStatusResponse blockUser(String id) {
        long userId = parseUserId(id);
        User user = blockUserHandler.handle(new BlockUserCommand(userId));
        return toUserStatusResponse(user);
    }

    public UserStatusResponse unblockUser(String id) {
        long userId = parseUserId(id);
        User user = unblockUserHandler.handle(new UnblockUserCommand(userId));
        return toUserStatusResponse(user);
    }

    public UserRoleResponse changeUserRole(String id, ChangeRoleRequest changeRoleRequest) {
        long userId = parseUserId(id);
        ChangeUserRoleCommand command = ChangeUserRoleCommand.builder()
                .userId(userId)
                .role(UserRole.fromString(changeRoleRequest.getRole().getValue()))
                .build();

        User user = changeUserRoleHandler.handle(command);

        UserRoleResponse response = new UserRoleResponse();
        response.setId(user.getId());
        response.setRole(UserRoleResponse.RoleEnum.fromValue(user.getRole().getValue()));
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    public InvitationResponse inviteUser(String id) {
        long userId = parseUserId(id);
        User user = inviteUserHandler.handle(new InviteUserCommand(userId));

        InvitationResponse response = new InvitationResponse();
        response.setId(user.getId());
        if (user.getInvitationSentAt() != null) {
            response.setInvitationSentAt(user.getInvitationSentAt());
        }
        response.setStatus(InvitationResponse.StatusEnum.PENDING);
        response.setMessage(MSG_INVITATION_SENT);
        return response;
    }

    private static UserStatusResponse toUserStatusResponse(User user) {
        UserStatusResponse response = new UserStatusResponse();
        response.setId(user.getId());
        response.setStatus(UserStatusResponse.StatusEnum.fromValue(user.getStatus().getValue()));
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    private static long parseUserId(String id) {
        try {
            return Long.parseLong(id);
        }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
