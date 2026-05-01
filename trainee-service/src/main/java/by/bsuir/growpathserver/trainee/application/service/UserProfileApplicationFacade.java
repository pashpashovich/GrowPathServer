package by.bsuir.growpathserver.trainee.application.service;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
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

        return userMapper.toUserProfileResponse(user, user.getAvatarUrl());
    }

    @Transactional
    public PresignAvatarUploadResponse createAvatarPresignedUpload() {
        Long userId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String token = UUID.randomUUID().toString();

        String uploadUrl = userAvatarStorageService.createPresignedUploadUrl(token);

        userEntity.setAvatarUrl(token);
        userRepository.save(userEntity);

        PresignAvatarUploadResponse response = new PresignAvatarUploadResponse();
        response.setObjectKey(token);
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

    public Resource downloadAvatar() {
        Long userId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (StringUtils.isBlank(userEntity.getAvatarUrl())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar not found");
        }

        return userAvatarStorageService.downloadFile(userEntity.getAvatarUrl());
    }
}
