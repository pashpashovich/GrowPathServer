package by.bsuir.growpathserver.trainee.application.service;

public interface TaskArtifactStorageService {
    String createPresignedUploadUrl(String objectKey);

    String createPresignedDownloadUrl(String objectKey);
}
