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
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    @Override
    public ResponseEntity<UserStatusResponse> blockUser(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<UserRoleResponse> changeUserRole(String id, ChangeRoleRequest changeRoleRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<UserResponse> createUser(CreateUserRequest createUserRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MessageResponse> deleteUser(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<UserListResponse> getUsers(Integer page,
                                                     Integer limit,
                                                     String role,
                                                     String status,
                                                     String search) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<InvitationResponse> inviteUser(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<UserStatusResponse> unblockUser(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(String id, UpdateUserRequest updateUserRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
