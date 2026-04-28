package by.bsuir.growpathserver.trainee.application.service;

public interface UserAvatarStorageService {
    String createPresignedUploadUrl(String objectKey);

    String createPresignedDownloadUrl(String objectKey);
}
