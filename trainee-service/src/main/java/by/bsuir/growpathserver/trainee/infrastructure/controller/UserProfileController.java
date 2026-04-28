package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ProfileApi;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.PresignAvatarUploadRequest;
import by.bsuir.growpathserver.dto.model.PresignAvatarUploadResponse;
import by.bsuir.growpathserver.dto.model.UserProfileResponse;
import by.bsuir.growpathserver.trainee.application.service.UserProfileApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserProfileController extends BaseController implements ProfileApi {

    private final UserProfileApplicationFacade userProfileApplicationFacade;

    @Override
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        return ResponseEntity.ok(userProfileApplicationFacade.getCurrentUserProfile());
    }

    @Override
    public ResponseEntity<PresignAvatarUploadResponse> presignAvatarUpload(PresignAvatarUploadRequest request) {
        return ResponseEntity.ok(userProfileApplicationFacade.createAvatarPresignedUpload(request.getFileName()));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteAvatar() {
        return ResponseEntity.ok(userProfileApplicationFacade.deleteAvatar());
    }
}
