package by.bsuir.growpathserver.trainee.application.service;

import org.springframework.core.io.Resource;

public interface UserAvatarStorageService {
    String createPresignedUploadUrl(String objectKey);

    Resource downloadFile(String objectKey);
}
