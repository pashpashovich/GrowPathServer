package by.bsuir.growpathserver.trainee.application.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.PresignAvatarUploadResponse;
import by.bsuir.growpathserver.dto.model.UserProfileResponse;
import by.bsuir.growpathserver.trainee.application.handler.GetCurrentUserProfileHandler;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.query.GetCurrentUserProfileQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.UserMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileApplicationFacade {

    private static final String MSG_AVATAR_DELETED = "Avatar deleted successfully";

    private final GetCurrentUserProfileHandler getCurrentUserProfileHandler;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;
    private final UserRepository userRepository;
    private final UserAvatarStorageService userAvatarStorageService;
    private final UserMapper userMapper;

    public UserProfileResponse getCurrentUserProfile() {
        GetCurrentUserProfileQuery query = new GetCurrentUserProfileQuery();
        User user = getCurrentUserProfileHandler.handle(query);

        String avatarPresignedUrl = null;
        if (StringUtils.isNotBlank(user.getAvatarUrl())) {
            avatarPresignedUrl = userAvatarStorageService.createPresignedDownloadUrl(user.getAvatarUrl());
        }

        return userMapper.toUserProfileResponse(user, avatarPresignedUrl);
    }

    @Transactional
    public PresignAvatarUploadResponse createAvatarPresignedUpload(String fileName) {
        Long userId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String safeName = StringUtils.defaultIfBlank(fileName, "avatar.jpg").replace(" ", "_");
        String objectKey = "avatars/%d/%d_%s".formatted(userId, System.currentTimeMillis(), safeName);
        String uploadUrl = userAvatarStorageService.createPresignedUploadUrl(objectKey);

        // Save objectKey immediately
        userEntity.setAvatarUrl(objectKey);
        userRepository.save(userEntity);

        PresignAvatarUploadResponse response = new PresignAvatarUploadResponse();
        response.setObjectKey(objectKey);
        response.setUploadUrl(uploadUrl);
        return response;
    }

    @Transactional
    public MessageResponse deleteAvatar() {
        Long userId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userEntity.setAvatarUrl(null);
        userRepository.save(userEntity);

        MessageResponse response = new MessageResponse();
        response.setMessage(MSG_AVATAR_DELETED);
        return response;
    }
}
