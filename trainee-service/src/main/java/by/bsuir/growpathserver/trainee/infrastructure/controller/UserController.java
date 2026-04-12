package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.UsersApi;
import by.bsuir.growpathserver.dto.model.ChangeRoleRequest;
import by.bsuir.growpathserver.dto.model.CreateUserRequest;
import by.bsuir.growpathserver.dto.model.InvitationResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateUserRequest;
import by.bsuir.growpathserver.dto.model.UserListResponse;
import by.bsuir.growpathserver.dto.model.UserResponse;
import by.bsuir.growpathserver.dto.model.UserRoleResponse;
import by.bsuir.growpathserver.dto.model.UserStatusResponse;
import by.bsuir.growpathserver.trainee.application.service.UsersApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController extends BaseController implements UsersApi {

    private final UsersApplicationFacade usersApplicationFacade;

    @Override
    public ResponseEntity<UserListResponse> getUsers(Integer page,
                                                     Integer limit,
                                                     String role,
                                                     String status,
                                                     String search) {
        return ResponseEntity.ok(usersApplicationFacade.listUsers(page, limit, role, status, search));
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(String id) {
        return ResponseEntity.ok(usersApplicationFacade.getUserById(id));
    }

    @Override
    public ResponseEntity<UserResponse> createUser(CreateUserRequest createUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usersApplicationFacade.createUser(createUserRequest));
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(String id, UpdateUserRequest updateUserRequest) {
        return ResponseEntity.ok(usersApplicationFacade.updateUser(id, updateUserRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteUser(String id) {
        return ResponseEntity.ok(usersApplicationFacade.deleteUser(id));
    }

    @Override
    public ResponseEntity<UserStatusResponse> blockUser(String id) {
        return ResponseEntity.ok(usersApplicationFacade.blockUser(id));
    }

    @Override
    public ResponseEntity<UserStatusResponse> unblockUser(String id) {
        return ResponseEntity.ok(usersApplicationFacade.unblockUser(id));
    }

    @Override
    public ResponseEntity<UserRoleResponse> changeUserRole(String id, ChangeRoleRequest changeRoleRequest) {
        return ResponseEntity.ok(usersApplicationFacade.changeUserRole(id, changeRoleRequest));
    }

    @Override
    public ResponseEntity<InvitationResponse> inviteUser(String id) {
        return ResponseEntity.ok(usersApplicationFacade.inviteUser(id));
    }
}
